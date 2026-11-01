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
  type    = string
  default = "us-east-2"
}

variable "app_name" {
  type    = string
  default = "autoflow-us"
}

variable "app_image_tag" {
  type    = string
  default = "latest"
}

variable "app_port" {
  type    = number
  default = 8080
}

variable "app_environment" {
  type      = map(string)
  default   = {}
  sensitive = true
}

resource "aws_ecr_repository" "app" {
  name                 = var.app_name
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

data "aws_vpc" "main" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.main.id]
  }

  filter {
    name   = "availability-zone"
    values = ["us-east-2a", "us-east-2b"]
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_iam_role" "eks_cluster" {
  name = "AutoFlowEksClusterRole"
}

data "aws_iam_role" "eks_nodes" {
  name = "AutoFlowEksNodeRole"
}

resource "aws_eks_cluster" "main" {
  name     = "${var.app_name}-eks"
  role_arn = data.aws_iam_role.eks_cluster.arn

  vpc_config {
    subnet_ids = data.aws_subnets.default.ids
  }
}

resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "${var.app_name}-nodes"
  node_role_arn   = data.aws_iam_role.eks_nodes.arn
  subnet_ids      = data.aws_subnets.default.ids

  instance_types = ["m7i-flex.large"]
  capacity_type  = "ON_DEMAND"

  scaling_config {
    desired_size = 1
    min_size     = 1
    max_size     = 2
  }
}

output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}

