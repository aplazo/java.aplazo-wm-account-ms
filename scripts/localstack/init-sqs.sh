#!/usr/bin/env bash

set -euo pipefail

echo "========================================="
echo " LocalStack init — wm-account-ms"
echo "========================================="

AWS_REGION=us-east-1
ACCOUNT_ID=000000000000

QUEUE_NAME=wm_customer_account_changes_local
TOPIC_NAME=customer_account_changes_local
QUEUE_ARN="arn:aws:sqs:${AWS_REGION}:${ACCOUNT_ID}:${QUEUE_NAME}"

# --------------------------------------------------------------------------
# 1. Create SQS queue (consumed by wm-account-ms listener)
# --------------------------------------------------------------------------
echo "[1/4] Creating SQS queue: ${QUEUE_NAME}"
QUEUE_URL=$(awslocal sqs create-queue \
  --queue-name "${QUEUE_NAME}" \
  --region "${AWS_REGION}" \
  --attributes VisibilityTimeout=30 \
  --query 'QueueUrl' \
  --output text)
echo "      Queue URL: ${QUEUE_URL}"

# --------------------------------------------------------------------------
# 2. Create SNS topic (published by customer-registration-service)
# --------------------------------------------------------------------------
echo "[2/4] Creating SNS topic: ${TOPIC_NAME}"
TOPIC_ARN=$(awslocal sns create-topic \
  --name "${TOPIC_NAME}" \
  --region "${AWS_REGION}" \
  --query 'TopicArn' \
  --output text)
echo "      Topic ARN: ${TOPIC_ARN}"

# --------------------------------------------------------------------------
# 3. Subscribe the SQS queue to the SNS topic (raw delivery — no SNS envelope)
# --------------------------------------------------------------------------
echo "[3/4] Subscribing SQS queue to SNS topic"
SUBSCRIPTION_ARN=$(awslocal sns subscribe \
  --topic-arn "${TOPIC_ARN}" \
  --protocol sqs \
  --notification-endpoint "${QUEUE_ARN}" \
  --region "${AWS_REGION}" \
  --query 'SubscriptionArn' \
  --output text)

awslocal sns set-subscription-attributes \
  --subscription-arn "${SUBSCRIPTION_ARN}" \
  --attribute-name RawMessageDelivery \
  --attribute-value true

echo "      Subscription ARN: ${SUBSCRIPTION_ARN}"

# --------------------------------------------------------------------------
# 4. Allow SNS to send messages to the SQS queue
# --------------------------------------------------------------------------
echo "[4/4] Setting SQS queue policy to allow SNS"

POLICY=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": { "Service": "sns.amazonaws.com" },
    "Action": "sqs:SendMessage",
    "Resource": "${QUEUE_ARN}",
    "Condition": {
      "ArnEquals": { "aws:SourceArn": "${TOPIC_ARN}" }
    }
  }]
}
EOF
)

awslocal sqs set-queue-attributes \
  --queue-url "${QUEUE_URL}" \
  --region "${AWS_REGION}" \
  --attributes "Policy=$(echo $POLICY | tr -d '\n')"

echo ""
echo "========================================="
echo " Setup complete"
echo " SQS Queue : ${QUEUE_URL}"
echo " SNS Topic : ${TOPIC_ARN}"
echo ""
echo " Set this env var in customer-registration-service:"
echo "   SNS_EVENT_CUSTOMER_ACCOUNT_CHANGE_PHONE_URL=${TOPIC_ARN}"
echo "========================================="
