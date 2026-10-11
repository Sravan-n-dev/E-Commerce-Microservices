terraform {
  required_version = ">= 1.5"
  backend "local" {}
}

provider "aws" {
  region = "us-east-1"
  access_key = "mock"
  secret_key = "mock"
  skip_credentials_validation = true
  skip_requesting_account_id = true
  endpoints {
    ecs = "http://localhost:4566"
    rds = "http://localhost:4566"
    elb = "http://localhost:4566"
    iam = "http://localhost:4566"
  }
}

resource "aws_ecs_cluster" "demo" {
  name = "microservices-demo"
}

resource "aws_ecs_task_definition" "api_gateway" {
  family                   = "api-gateway"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = "arn:aws:iam::123456789012:role/ecsTaskExecutionRole"
  container_definitions = jsonencode([
    {
      name      = "api-gateway"
      image     = "local/api-gateway:latest"
      cpu       = 256
      memory    = 512
      essential = true
      portMappings = [{ containerPort = 8080, hostPort = 8080 }]
    }
  ])
}

resource "aws_ecs_service" "api_gateway" {
  name            = "api-gateway"
  cluster         = aws_ecs_cluster.demo.id
  task_definition = aws_ecs_task_definition.api_gateway.arn
  launch_type     = "FARGATE"
  desired_count   = 1
  network_configuration {
    subnets         = ["subnet-12345"]
    assign_public_ip = true
  }
}
