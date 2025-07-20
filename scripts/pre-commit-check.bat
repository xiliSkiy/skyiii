@echo off
REM SkyEye 智能监控系统 - Git提交前检查脚本 (Windows版本)

echo 🔍 开始执行提交前检查...

REM 检查是否在项目根目录
if not exist "skyeye-frontend" if not exist "skyeye-backend" (
    echo ❌ 请在项目根目录执行此脚本
    exit /b 1
)

REM 前端检查
if exist "skyeye-frontend" (
    echo 📦 检查前端项目...
    cd skyeye-frontend
    
    REM 检查是否有.env文件被意外添加
    git ls-files | findstr "\.env$" >nul
    if %errorlevel% equ 0 (
        echo ❌ 发现.env文件在版本控制中，请移除
        echo 运行: git rm --cached .env
        exit /b 1
    )
    
    REM 运行代码检查
    echo 🔧 运行ESLint检查...
    call npm run lint
    if %errorlevel% neq 0 (
        echo ❌ ESLint检查失败，请修复代码问题
        exit /b 1
    )
    
    REM 运行类型检查
    echo 📝 运行TypeScript类型检查...
    call npm run type-check
    if %errorlevel% neq 0 (
        echo ❌ TypeScript类型检查失败
        exit /b 1
    )
    
    REM 运行单元测试
    echo 🧪 运行前端单元测试...
    call npm run test:unit -- --run
    if %errorlevel% neq 0 (
        echo ❌ 前端单元测试失败
        exit /b 1
    )
    
    cd ..
)

REM 后端检查
if exist "skyeye-backend" (
    echo ☕ 检查后端项目...
    cd skyeye-backend
    
    REM 检查是否有敏感文件被意外添加
    git ls-files | findstr "application-.*\.properties$" >nul
    if %errorlevel% equ 0 (
        echo ❌ 发现敏感配置文件在版本控制中
        exit /b 1
    )
    
    REM 运行代码编译
    echo 🔨 编译后端代码...
    call mvn compile -q
    if %errorlevel% neq 0 (
        echo ❌ 后端代码编译失败
        exit /b 1
    )
    
    REM 运行单元测试
    echo 🧪 运行后端单元测试...
    call mvn test -q
    if %errorlevel% neq 0 (
        echo ❌ 后端单元测试失败
        exit /b 1
    )
    
    cd ..
)

echo ✅ 所有检查通过，可以提交代码
echo 🚀 建议的提交命令:
echo git add .
echo git commit -m "feat: your commit message"
echo git push origin main