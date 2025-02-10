#!/bin/bash

# Verificar que se pasen los argumentos necesarios
if [ "$#" -ne 3 ]; then
    echo "Uso: $0 <FILE_PATH> <IMAGE_VERSION> <SERVICES>"
    exit 1
fi

ROLE_ARN=$1
SESSION_NAME=$2
ECR=$3

# Asumir el rol y obtener las credenciales
 CREDS=$(aws sts assume-role --role-arn $ROLE_ARN --role-session-name $SESSION_NAME --output json 2>/dev/null)
                                
AWS_ACCESS_KEY_ID=$(echo $CREDS | jq -r .Credentials.AccessKeyId 2>/dev/null)
AWS_SECRET_ACCESS_KEY=$(echo $CREDS | jq -r .Credentials.SecretAccessKey 2>/dev/null)
AWS_SESSION_TOKEN=$(echo $CREDS | jq -r .Credentials.SessionToken 2>/dev/null)

# Exportar las credenciales
export AWS_ACCESS_KEY_ID
export AWS_SECRET_ACCESS_KEY
export AWS_SESSION_TOKEN

# Iniciar sesión en ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $ECR
