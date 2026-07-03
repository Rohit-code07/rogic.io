# --- AWS Cognito User Pool for Staging ---
resource "aws_cognito_user_pool" "user_pool" {
  name                     = "nemologic-staging-user-pool-${random_string.suffix.result}"
  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  password_policy {
    minimum_length    = 8
    require_lowercase = false
    require_numbers   = false
    require_symbols   = false
    require_uppercase = false
  }

  schema {
    attribute_data_type      = "String"
    developer_only_attribute = false
    mutable                  = true
    name                     = "email"
    required                 = true

    string_attribute_constraints {
      min_length = 0
      max_length = 2048
    }
  }

  tags = {
    Name = "nemologic-staging-user-pool"
  }
}

# --- Google Identity Provider Integration ---
resource "aws_cognito_identity_provider" "google_provider" {
  user_pool_id  = aws_cognito_user_pool.user_pool.id
  provider_name = "Google"
  provider_type = "Google"

  provider_details = {
    client_id        = var.google_oauth_client_id
    client_secret    = var.google_oauth_client_secret
    authorize_scopes = "profile email openid"
  }

  attribute_mapping = {
    email    = "email"
    name     = "name"
    username = "sub"
    picture  = "picture"
  }

  lifecycle {
    ignore_changes = [
      provider_details["client_id"],
      provider_details["client_secret"]
    ]
  }
}

# --- Cognito App Client for Frontend Single Page App ---
resource "aws_cognito_user_pool_client" "client" {
  name                                 = "nemologic-staging-client-${random_string.suffix.result}"
  user_pool_id                         = aws_cognito_user_pool.user_pool.id
  generate_secret                      = false
  supported_identity_providers         = ["Google"]
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = ["email", "openid", "profile"]

  callback_urls = [
    "http://localhost:5173/",
    "https://stage.rogic.io/"
  ]

  logout_urls = [
    "http://localhost:5173/",
    "https://stage.rogic.io/"
  ]

  access_token_validity  = 5
  id_token_validity      = 5
  refresh_token_validity = 30

  token_validity_units {
    access_token  = "minutes"
    id_token      = "minutes"
    refresh_token = "days"
  }

  enable_token_revocation = true

  refresh_token_rotation {
    feature                    = "ENABLED"
    retry_grace_period_seconds = 0
  }

  depends_on = [
    aws_cognito_identity_provider.google_provider
  ]
}

# --- Cognito Domain for Hosted UI ---
resource "aws_cognito_user_pool_domain" "domain" {
  domain       = "nemologic-stage-auth-${random_string.suffix.result}"
  user_pool_id = aws_cognito_user_pool.user_pool.id
}
