# PowerShell script to deploy ForkIt API to Azure
# Run this script from the API/forkit-api directory

param(
    [Parameter(Mandatory=$true)]
    [string]$ResourceGroupName,
    
    [Parameter(Mandatory=$true)]
    [string]$AppName,
    
    [Parameter(Mandatory=$false)]
    [string]$Location = "East US"
)

Write-Host "Deploying ForkIt API to Azure..." -ForegroundColor Green

# Check if Azure CLI is installed
try {
    $azVersion = az version 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Azure CLI not found"
    }
    Write-Host "Azure CLI found" -ForegroundColor Green
} catch {
    Write-Host "Azure CLI not found. Please install it from: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli" -ForegroundColor Red
    exit 1
}

# Login to Azure (if not already logged in)
Write-Host "Checking Azure login status..." -ForegroundColor Yellow
$loginStatus = az account show 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Please login to Azure..." -ForegroundColor Yellow
    az login
}

# Create resource group if it doesn't exist
Write-Host "Creating resource group: $ResourceGroupName" -ForegroundColor Yellow
az group create --name $ResourceGroupName --location $Location

# Create App Service plan
Write-Host "Creating App Service plan..." -ForegroundColor Yellow
az appservice plan create --name "$AppName-plan" --resource-group $ResourceGroupName --sku FREE --is-linux

# Create web app
Write-Host "Creating web app: $AppName" -ForegroundColor Yellow
az webapp create --resource-group $ResourceGroupName --plan "$AppName-plan" --name $AppName --runtime "NODE:18"

# Configure app settings
Write-Host "Configuring app settings..." -ForegroundColor Yellow
az webapp config appsettings set --resource-group $ResourceGroupName --name $AppName --settings NODE_ENV=production

Write-Host "Deployment configured successfully!" -ForegroundColor Green
Write-Host "Your API will be available at: https://$AppName.azurewebsites.net" -ForegroundColor Cyan
Write-Host "Health check: https://$AppName.azurewebsites.net/api/health" -ForegroundColor Cyan
Write-Host "Manual deployment required. Please follow the steps in azure-deploy.md" -ForegroundColor Yellow

Write-Host "Deployment process completed!" -ForegroundColor Green
Write-Host "For detailed instructions, see azure-deploy.md" -ForegroundColor Cyan