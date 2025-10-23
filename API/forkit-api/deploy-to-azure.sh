#!/bin/bash

# Bash script to deploy ForkIt API to Azure
# Run this script from the API/forkit-api directory

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${CYAN}🔍 $1${NC}"
}

# Check if parameters are provided
if [ $# -lt 2 ]; then
    echo "Usage: ./deploy-to-azure.sh <ResourceGroupName> <AppName> [Location]"
    echo "Example: ./deploy-to-azure.sh forkit-rg forkit-api East US"
    exit 1
fi

RESOURCE_GROUP=$1
APP_NAME=$2
LOCATION=${3:-"East US"}

echo -e "${GREEN}🚀 Deploying ForkIt API to Azure...${NC}"

# Check if Azure CLI is installed
if ! command -v az &> /dev/null; then
    print_error "Azure CLI not found. Please install it from: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
    exit 1
fi
print_status "Azure CLI found"

# Login to Azure (if not already logged in)
print_warning "Checking Azure login status..."
if ! az account show &> /dev/null; then
    print_warning "Please login to Azure..."
    az login
fi

# Create resource group if it doesn't exist
print_warning "Creating resource group: $RESOURCE_GROUP"
az group create --name $RESOURCE_GROUP --location "$LOCATION"

# Create App Service plan
print_warning "Creating App Service plan..."
az appservice plan create --name "$APP_NAME-plan" --resource-group $RESOURCE_GROUP --sku FREE --is-linux

# Create web app
print_warning "Creating web app: $APP_NAME"
az webapp create --resource-group $RESOURCE_GROUP --plan "$APP_NAME-plan" --name $APP_NAME --runtime "NODE:18-lts"

# Configure app settings
print_warning "Configuring app settings..."
az webapp config appsettings set --resource-group $RESOURCE_GROUP --name $APP_NAME --settings NODE_ENV=production

print_status "Deployment configured successfully!"
print_info "Your API will be available at: https://$APP_NAME.azurewebsites.net"
print_info "Health check: https://$APP_NAME.azurewebsites.net/api/health"

echo -e "${GREEN}🎉 Deployment process completed!${NC}"
echo -e "${CYAN}📖 For detailed instructions, see azure-deploy.md${NC}"
