# Azure Deployment Guide for ForkIt API

## Prerequisites

1. **Azure Account**: Sign up at [portal.azure.com](https://portal.azure.com)
2. **Azure CLI**: Install from [docs.microsoft.com](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)
3. **Git**: For deployment integration

## Step 1: Create Azure App Service

### Option A: Using Azure Portal (Recommended for beginners)

1. Go to [Azure Portal](https://portal.azure.com)
2. Click "Create a resource" → "Web App"
3. Fill in the details:
   - **Resource Group**: Create new or use existing
   - **Name**: `forkit-api-[your-unique-name]`
   - **Runtime stack**: Node 18 LTS
   - **Operating System**: Linux
   - **Region**: Choose closest to your users
   - **Pricing Plan**: Free F1 (for testing) or Basic B1 (for production)

### Option B: Using Azure CLI

```bash
# Login to Azure
az login

# Create resource group
az group create --name forkit-rg --location "East US"

# Create App Service plan
az appservice plan create --name forkit-plan --resource-group forkit-rg --sku FREE

# Create web app
az webapp create --resource-group forkit-rg --plan forkit-plan --name forkit-api-[your-unique-name] --runtime "NODE:18-lts"
```

## Step 2: Configure Environment Variables

1. In Azure Portal, go to your App Service
2. Navigate to "Configuration" → "Application settings"
3. Add the following environment variables:

```
NODE_ENV=production
PORT=8080
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_PRIVATE_KEY=your-firebase-private-key
FIREBASE_CLIENT_EMAIL=your-firebase-client-email
```

## Step 3: Deploy Your Code

### Option A: Git Deployment (Recommended)

1. In Azure Portal, go to your App Service
2. Navigate to "Deployment Center"
3. Choose "Local Git" as source
4. Copy the Git URL provided
5. In your local terminal:

```bash
cd API/forkit-api
git remote add azure <your-azure-git-url>
git add .
git commit -m "Azure deployment"
git push azure main
```

### Option B: ZIP Deployment

1. Create a ZIP file of your API folder (excluding node_modules)
2. In Azure Portal, go to "Advanced Tools" → "Kudu"
3. Navigate to "Site extensions" → "Zip deployment"
4. Upload your ZIP file

### Option C: Azure CLI Deployment

```bash
# Install Azure CLI extension for web apps
az extension add --name webapp

# Deploy from local folder
az webapp deployment source config-zip --resource-group forkit-rg --name forkit-api-[your-name] --src forkit-api.zip
```

## Step 4: Configure Custom Domain (Optional)

1. In Azure Portal, go to your App Service
2. Navigate to "Custom domains"
3. Add your domain and configure DNS records
4. Enable SSL certificate

## Step 5: Monitor and Scale

1. **Application Insights**: Enable for monitoring
2. **Logs**: View in "Log stream" or "Logs" section
3. **Scaling**: Configure in "Scale out" section

## Troubleshooting

### Common Issues:

1. **Port Issues**: Ensure your app listens on `process.env.PORT`
2. **Environment Variables**: Check all required variables are set
3. **Firebase**: Verify Firebase configuration
4. **Dependencies**: Ensure all npm packages are in package.json

### Useful Commands:

```bash
# View logs
az webapp log tail --resource-group forkit-rg --name forkit-api-[your-name]

# Restart app
az webapp restart --resource-group forkit-rg --name forkit-api-[your-name]

# Get app URL
az webapp show --resource-group forkit-rg --name forkit-api-[your-name] --query defaultHostName
```

## Testing Your Deployment

1. Visit your app URL: `https://forkit-api-[your-name].azurewebsites.net`
2. Test health endpoint: `https://forkit-api-[your-name].azurewebsites.net/api/health`
3. Test your mobile app with the new Azure URL

## Migration from Render

1. Update your mobile app's API base URL
2. Test all endpoints thoroughly
3. Update any documentation
4. Consider gradual migration with feature flags
