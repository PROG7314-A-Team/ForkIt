@echo off
echo Starting ForkIt API on Windows...
echo Node version:
node --version
echo NPM version:
npm --version
echo Installing dependencies...
call npm install --production
echo Starting application...
node src/server.js
