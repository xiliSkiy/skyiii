#!/bin/bash

# SkyEye 智能监控系统 - Git提交前检查脚本

echo "🔍 开始执行提交前检查..."

# 检查是否在项目根目录
if [ ! -f "package.json" ] && [ ! -f "pom.xml" ]; then
    echo "❌ 请在项目根目录执行此脚本"
    exit 1
fi

# 前端检查
if [ -d "skyeye-frontend" ]; then
    echo "📦 检查前端项目..."
    cd skyeye-frontend
    
    # 检查是否有.env文件被意外添加
    if git ls-files | grep -q "\.env$"; then
        echo "❌ 发现.env文件在版本控制中，请移除"
        echo "运行: git rm --cached .env"
        exit 1
    fi
    
    # 运行代码检查
    echo "🔧 运行ESLint检查..."
    npm run lint
    if [ $? -ne 0 ]; then
        echo "❌ ESLint检查失败，请修复代码问题"
        exit 1
    fi
    
    # 运行类型检查
    echo "📝 运行TypeScript类型检查..."
    npm run type-check
    if [ $? -ne 0 ]; then
        echo "❌ TypeScript类型检查失败"
        exit 1
    fi
    
    # 运行单元测试
    echo "🧪 运行前端单元测试..."
    npm run test:unit -- --run
    if [ $? -ne 0 ]; then
        echo "❌ 前端单元测试失败"
        exit 1
    fi
    
    cd ..
fi

# 后端检查
if [ -d "skyeye-backend" ]; then
    echo "☕ 检查后端项目..."
    cd skyeye-backend
    
    # 检查是否有敏感文件被意外添加
    if git ls-files | grep -q "application-.*\.properties$"; then
        echo "❌ 发现敏感配置文件在版本控制中"
        exit 1
    fi
    
    # 运行代码编译
    echo "🔨 编译后端代码..."
    mvn compile -q
    if [ $? -ne 0 ]; then
        echo "❌ 后端代码编译失败"
        exit 1
    fi
    
    # 运行单元测试
    echo "🧪 运行后端单元测试..."
    mvn test -q
    if [ $? -ne 0 ]; then
        echo "❌ 后端单元测试失败"
        exit 1
    fi
    
    cd ..
fi

# 检查提交信息格式（如果有的话）
if [ ! -z "$1" ]; then
    commit_msg="$1"
    if ! echo "$commit_msg" | grep -qE "^(feat|fix|docs|style|refactor|test|chore)(\(.+\))?: .+"; then
        echo "❌ 提交信息格式不正确"
        echo "正确格式: type(scope): description"
        echo "例如: feat(auth): add user login functionality"
        exit 1
    fi
fi

echo "✅ 所有检查通过，可以提交代码"
echo "🚀 建议的提交命令:"
echo "git add ."
echo "git commit -m 'feat: your commit message'"
echo "git push origin main"