/**
 * SkyEye 智能监控系统 - 自定义脚本
 */

// 文档加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    console.log('SkyEye系统脚本已加载');
    
    // 初始化侧边栏折叠功能
    initSidebar();
    
    // 初始化下拉菜单
    initDropdowns();
    
    // 初始化工具提示
    initTooltips();
    
    // 初始化表格排序
    initTableSort();
    
    // 模拟数据加载
    simulateDataLoading();
});

/**
 * 初始化侧边栏折叠功能
 */
function initSidebar() {
    // 查找所有带有data-toggle="collapse"属性的元素
    const collapseTriggers = document.querySelectorAll('[data-toggle="collapse"]');
    
    // 为每个触发器添加点击事件
    collapseTriggers.forEach(trigger => {
        trigger.addEventListener('click', function(e) {
            e.preventDefault();
            
            // 获取目标元素
            const targetId = this.getAttribute('href');
            const targetElement = document.querySelector(targetId);
            
            // 切换展开/折叠状态
            if (targetElement) {
                if (targetElement.classList.contains('show')) {
                    targetElement.classList.remove('show');
                    this.setAttribute('aria-expanded', 'false');
                } else {
                    targetElement.classList.add('show');
                    this.setAttribute('aria-expanded', 'true');
                }
            }
        });
    });
}

/**
 * 初始化下拉菜单
 */
function initDropdowns() {
    // 查找所有下拉菜单触发器
    const dropdownTriggers = document.querySelectorAll('[data-toggle="dropdown"]');
    
    // 为每个触发器添加点击事件
    dropdownTriggers.forEach(trigger => {
        trigger.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            // 获取父元素和下拉菜单
            const parent = this.parentElement;
            const dropdown = parent.querySelector('.dropdown-menu');
            
            // 切换下拉菜单显示状态
            if (dropdown) {
                dropdown.classList.toggle('show');
            }
        });
    });
    
    // 点击页面其他地方关闭所有下拉菜单
    document.addEventListener('click', function() {
        const openDropdowns = document.querySelectorAll('.dropdown-menu.show');
        openDropdowns.forEach(dropdown => {
            dropdown.classList.remove('show');
        });
    });
}

/**
 * 初始化工具提示
 */
function initTooltips() {
    // 查找所有带有title属性的元素
    const tooltipElements = document.querySelectorAll('[title]');
    
    // 为每个元素添加鼠标悬停事件
    tooltipElements.forEach(element => {
        const title = element.getAttribute('title');
        element.setAttribute('data-original-title', title);
        element.removeAttribute('title');
        
        // 鼠标悬停显示工具提示
        element.addEventListener('mouseenter', function() {
            // 创建工具提示元素
            const tooltip = document.createElement('div');
            tooltip.className = 'tooltip';
            tooltip.textContent = this.getAttribute('data-original-title');
            
            // 计算位置
            const rect = this.getBoundingClientRect();
            tooltip.style.position = 'absolute';
            tooltip.style.top = (rect.top - 30) + 'px';
            tooltip.style.left = (rect.left + rect.width / 2) + 'px';
            tooltip.style.transform = 'translateX(-50%)';
            
            // 添加到页面
            document.body.appendChild(tooltip);
            this._tooltip = tooltip;
        });
        
        // 鼠标移出隐藏工具提示
        element.addEventListener('mouseleave', function() {
            if (this._tooltip) {
                document.body.removeChild(this._tooltip);
                this._tooltip = null;
            }
        });
    });
}

/**
 * 初始化表格排序
 */
function initTableSort() {
    // 查找所有可排序表格
    const sortableTables = document.querySelectorAll('table.sortable');
    
    sortableTables.forEach(table => {
        const headers = table.querySelectorAll('th[data-sort]');
        
        headers.forEach(header => {
            header.addEventListener('click', function() {
                const sortKey = this.getAttribute('data-sort');
                const sortDirection = this.getAttribute('data-direction') || 'asc';
                
                // 更新排序方向
                const newDirection = sortDirection === 'asc' ? 'desc' : 'asc';
                this.setAttribute('data-direction', newDirection);
                
                // 获取表格数据
                const tbody = table.querySelector('tbody');
                const rows = Array.from(tbody.querySelectorAll('tr'));
                
                // 排序行
                rows.sort((a, b) => {
                    const aValue = a.querySelector(`td[data-${sortKey}]`).getAttribute(`data-${sortKey}`);
                    const bValue = b.querySelector(`td[data-${sortKey}]`).getAttribute(`data-${sortKey}`);
                    
                    if (newDirection === 'asc') {
                        return aValue.localeCompare(bValue);
                    } else {
                        return bValue.localeCompare(aValue);
                    }
                });
                
                // 重新添加排序后的行
                rows.forEach(row => tbody.appendChild(row));
            });
        });
    });
}

/**
 * 模拟数据加载
 */
function simulateDataLoading() {
    // 查找所有数据加载容器
    const loadingContainers = document.querySelectorAll('[data-loading]');
    
    loadingContainers.forEach(container => {
        // 显示加载动画
        const loadingSpinner = document.createElement('div');
        loadingSpinner.className = 'spinner-border text-primary';
        loadingSpinner.setAttribute('role', 'status');
        
        const loadingText = document.createElement('span');
        loadingText.className = 'sr-only';
        loadingText.textContent = '加载中...';
        
        loadingSpinner.appendChild(loadingText);
        container.appendChild(loadingSpinner);
        
        // 模拟延迟加载
        setTimeout(() => {
            // 移除加载动画
            container.removeChild(loadingSpinner);
            
            // 显示数据内容
            const dataContent = container.querySelector('[data-content]');
            if (dataContent) {
                dataContent.style.display = 'block';
            }
        }, 1000);
    });
}

/**
 * 切换密码可见性
 * @param {string} inputId - 密码输入框ID
 * @param {string} iconId - 图标元素ID
 */
function togglePasswordVisibility(inputId, iconId) {
    const passwordInput = document.getElementById(inputId);
    const icon = document.getElementById(iconId);
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
    }
}

/**
 * 显示通知消息
 * @param {string} message - 消息内容
 * @param {string} type - 消息类型 (success, warning, danger, info)
 * @param {number} duration - 显示时长(毫秒)
 */
function showNotification(message, type = 'info', duration = 3000) {
    // 创建通知元素
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    // 添加到页面
    document.body.appendChild(notification);
    
    // 显示通知
    setTimeout(() => {
        notification.classList.add('show');
    }, 10);
    
    // 设置自动关闭
    setTimeout(() => {
        notification.classList.remove('show');
        
        // 动画结束后移除元素
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, duration);
}

/**
 * 确认对话框
 * @param {string} message - 确认消息
 * @param {function} onConfirm - 确认回调
 * @param {function} onCancel - 取消回调
 */
function confirmDialog(message, onConfirm, onCancel) {
    // 创建对话框背景
    const overlay = document.createElement('div');
    overlay.className = 'confirm-overlay';
    
    // 创建对话框
    const dialog = document.createElement('div');
    dialog.className = 'confirm-dialog';
    
    // 创建消息
    const messageElement = document.createElement('p');
    messageElement.textContent = message;
    
    // 创建按钮容器
    const buttonContainer = document.createElement('div');
    buttonContainer.className = 'confirm-buttons';
    
    // 创建确认按钮
    const confirmButton = document.createElement('button');
    confirmButton.className = 'btn btn-primary';
    confirmButton.textContent = '确认';
    confirmButton.addEventListener('click', () => {
        document.body.removeChild(overlay);
        if (typeof onConfirm === 'function') {
            onConfirm();
        }
    });
    
    // 创建取消按钮
    const cancelButton = document.createElement('button');
    cancelButton.className = 'btn btn-secondary';
    cancelButton.textContent = '取消';
    cancelButton.addEventListener('click', () => {
        document.body.removeChild(overlay);
        if (typeof onCancel === 'function') {
            onCancel();
        }
    });
    
    // 组装对话框
    buttonContainer.appendChild(cancelButton);
    buttonContainer.appendChild(confirmButton);
    dialog.appendChild(messageElement);
    dialog.appendChild(buttonContainer);
    overlay.appendChild(dialog);
    
    // 添加到页面
    document.body.appendChild(overlay);
}

/**
 * 格式化日期时间
 * @param {Date|string} date - 日期对象或日期字符串
 * @param {string} format - 格式模板
 * @returns {string} 格式化后的日期字符串
 */
function formatDateTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
    if (!(date instanceof Date)) {
        date = new Date(date);
    }
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds);
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的文件大小
 */
function formatFileSize(bytes) {
    if (bytes === 0) return '0 B';
    
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * 防抖函数
 * @param {function} func - 要执行的函数
 * @param {number} wait - 等待时间(毫秒)
 * @returns {function} 防抖处理后的函数
 */
function debounce(func, wait) {
    let timeout;
    
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * 节流函数
 * @param {function} func - 要执行的函数
 * @param {number} limit - 限制时间(毫秒)
 * @returns {function} 节流处理后的函数
 */
function throttle(func, limit) {
    let inThrottle;
    
    return function executedFunction(...args) {
        if (!inThrottle) {
            func(...args);
            inThrottle = true;
            
            setTimeout(() => {
                inThrottle = false;
            }, limit);
        }
    };
}

/**
 * 表单验证
 * @param {HTMLFormElement} form - 表单元素
 * @returns {boolean} 验证是否通过
 */
function validateForm(form) {
    // 获取所有必填字段
    const requiredFields = form.querySelectorAll('[required]');
    let isValid = true;
    
    // 检查每个必填字段
    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            // 字段为空，添加错误样式
            field.classList.add('is-invalid');
            isValid = false;
            
            // 创建错误消息
            const errorMessage = document.createElement('div');
            errorMessage.className = 'invalid-feedback';
            errorMessage.textContent = '此字段不能为空';
            
            // 添加错误消息
            const parent = field.parentElement;
            if (!parent.querySelector('.invalid-feedback')) {
                parent.appendChild(errorMessage);
            }
        } else {
            // 字段有值，移除错误样式
            field.classList.remove('is-invalid');
            field.classList.add('is-valid');
            
            // 移除错误消息
            const errorMessage = field.parentElement.querySelector('.invalid-feedback');
            if (errorMessage) {
                errorMessage.parentElement.removeChild(errorMessage);
            }
        }
    });
    
    return isValid;
}

/**
 * 提交表单数据
 * @param {HTMLFormElement} form - 表单元素
 * @param {string} url - 提交地址
 * @param {function} callback - 回调函数
 */
function submitFormData(form, url, callback) {
    // 验证表单
    if (!validateForm(form)) {
        return;
    }
    
    // 收集表单数据
    const formData = new FormData(form);
    const data = {};
    
    for (const [key, value] of formData.entries()) {
        data[key] = value;
    }
    
    // 模拟提交
    console.log('提交数据到', url, data);
    
    // 模拟延迟响应
    setTimeout(() => {
        // 调用回调函数
        if (typeof callback === 'function') {
            callback({
                success: true,
                message: '数据提交成功'
            });
        }
    }, 1000);
} 