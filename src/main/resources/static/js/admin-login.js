document.addEventListener('DOMContentLoaded', () => {
    const adminLoginForm = document.getElementById('adminLoginForm');
    const messageDiv = document.getElementById('message');

    adminLoginForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        messageDiv.textContent = '';
        messageDiv.className = '';

        const formData = new FormData(adminLoginForm);
        const username = formData.get('username');
        const password = formData.get('password');

        try {
            const response = await fetch('/api/admin/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: username,
                    password: password
                })
            });

            const result = await response.json();

            if (response.ok) {
                // 登录成功，跳转到管理员仪表盘
                localStorage.setItem('adminToken', result.token);
                window.location.href = 'admin-dashboard.html';
            } else {
                messageDiv.textContent = '登录失败: ' + (result.message || '未知错误');
                messageDiv.classList.add('error');
            }
        } catch (error) {
            messageDiv.textContent = '网络错误或服务器无响应。';
            messageDiv.classList.add('error');
            console.error('登录时发生错误:', error);
        }
    });
});