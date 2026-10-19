terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

variable "aws_region" {
  description = "Região da AWS"
  type        = string
  default     = "us-east-1"
}

variable "app_name" {
  description = "Nome do repositório ECR já criado"
  type        = string
  default     = "autoflow-us"
}

variable "app_image_tag" {
  description = "Tag da imagem que o GitHub Action publicou no ECR"
  type        = string
  default     = "latest"
}

variable "app_port" {
  description = "Porta da aplicação"
  type        = number
  default     = 8080
}

variable "app_environment" {
  description = "Variáveis de ambiente da aplicação (DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET, etc.)"
  type        = map(string)
  default     = {}
  sensitive   = true
}

data "aws_ecr_repository" "app" {
  name = var.app_name
}

resource "aws_iam_role" "apprunner_ecr_access" {
  name = "${var.app_name}-apprunner-ecr"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "build.apprunner.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "apprunner_ecr_access" {
  name = "${var.app_name}-apprunner-ecr-policy"
  role = aws_iam_role.apprunner_ecr_access.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:DescribeImages",
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability"
      ]
      Resource = "*"
    }]
  })
}

resource "aws_apprunner_service" "app" {
  service_name = "${var.app_name}-api"

  source_configuration {
    auto_deployments_enabled = false

    authentication_configuration {
      access_role_arn = aws_iam_role.apprunner_ecr_access.arn
    }

    image_repository {
      image_identifier      = "${data.aws_ecr_repository.app.repository_url}:${var.app_image_tag}"
      image_repository_type = "ECR"

      image_configuration {
        port = var.app_port

        runtime_environment_variables = merge(var.app_environment, {
          SERVER_PORT = tostring(var.app_port)
        })
      }
    }
  }

  instance_configuration {
    cpu    = "1024"
    memory = "2048"
  }

  health_check_configuration {
    protocol = "HTTP"
    path     = "/swagger-ui/index.html"
  }
}

output "ecr_repository_url" {
  description = "URL do repositório ECR"
  value       = data.aws_ecr_repository.app.repository_url
}

output "app_url" {
  description = "URL pública da aplicação"
  value       = "https://${aws_apprunner_service.app.service_url}"
}
