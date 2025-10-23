# 🚀 Quick Start: Deploy ForkIt API to Azure

## Prerequisites Checklist
- [ ] Azure account (free tier available)
- [ ] Azure CLI installed
- [ ] Your Firebase project credentials
- [ ] Git repository access

## 🎯 Quick Deployment (5 minutes)

### Step 1: Install Azure CLI
```bash
# Windows (PowerShell as Administrator)
winget install Microsoft.AzureCLI

# Or download from: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli
```

### Step 2: Login to Azure
```bash
az login
```

### Step 3: Deploy Your API
```bash
# Navigate to your API directory
cd API/forkit-api

# Run the deployment script (Windows PowerShell)
.\deploy-to-azure.ps1 -ResourceGroupName "forkit-rg" -AppName "forkit-api-yourname" -Location "East US"

# Or run the bash script (Linux/Mac/WSL)
./deploy-to-azure.sh forkit-rg forkit-api-yourname "East US"
```

### Step 4: Configure Environment Variables
1. Go to [Azure Portal](https://portal.azure.com)
2. Find your App Service → Configuration → Application settings
3. Add these variables (use your actual Firebase values):

```
NODE_ENV=production
PORT=8080
FIREBASE_PROJECT_ID=your-actual-project-id
FIREBASE_PRIVATE_KEY=your-actual-private-key
FIREBASE_CLIENT_EMAIL=your-actual-client-email
```

### Step 5: Deploy Your Code
```bash
# Option A: Git deployment (recommended)
git remote add azure https://forkit-api-yourname.scm.azurewebsites.net:443/forkit-api-yourname.git
git push azure main

# Option B: ZIP deployment
# Create a ZIP of your API folder and upload via Azure Portal
```

## 🧪 Test Your Deployment

1. **Health Check**: `https://forkit-api-yourname.azurewebsites.net/api/health`
2. **API Root**: `https://forkit-api-yourname.azurewebsites.net/`
3. **Test with your mobile app**

## 🔄 Update Your Mobile App

Update your mobile app's API base URL from Render to Azure:
```kotlin
// In your mobile app's configuration
const val API_BASE_URL = "https://forkit-api-yourname.azurewebsites.net"
```

## 🆘 Troubleshooting

### Common Issues:
1. **"Application Error"**: Check environment variables
2. **"Cannot find module"**: Ensure all dependencies are in package.json
3. **Firebase errors**: Verify Firebase configuration
4. **Port issues**: App should listen on `process.env.PORT`

### Debug Commands:
```bash
# View logs
az webapp log tail --resource-group forkit-rg --name forkit-api-yourname

# Restart app
az webapp restart --resource-group forkit-rg --name forkit-api-yourname

# Get app URL
az webapp show --resource-group forkit-rg --name forkit-api-yourname --query defaultHostName
```

## 💰 Cost Management

- **Free Tier**: 1 GB RAM, 1 GB storage, 60 minutes/day
- **Basic Tier**: $13/month for production use
- **Monitor usage**: Azure Portal → Cost Management

## 🔐 Security Best Practices

1. **Environment Variables**: Never commit secrets to Git
2. **HTTPS**: Enabled by default on Azure
3. **Firewall**: Configure if needed
4. **Monitoring**: Enable Application Insights

## 📞 Need Help?

1. Check the detailed guide: `azure-deploy.md`
2. Azure documentation: [docs.microsoft.com](https://docs.microsoft.com/en-us/azure/app-service/)
3. ForkIt API documentation: `API_ENDPOINTS_DOCUMENTATION.md`

---
**🎉 Congratulations!** Your ForkIt API is now running on Azure!
