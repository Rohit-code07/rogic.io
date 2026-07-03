variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "ap-northeast-2"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3a.nano"
}

variable "key_name" {
  description = "Name of the existing SSH key pair in AWS"
  type        = string
  default     = "nemologic-key"
}

variable "alert_email" {
  description = "Email address to receive Gemini API failure alerts"
  type        = string
}

variable "google_oauth_client_id" {
  description = "Client ID for Google OAuth"
  type        = string
  default     = ""
}

variable "google_oauth_client_secret" {
  description = "Client Secret for Google OAuth"
  type        = string
  default     = ""
  sensitive   = true
}

