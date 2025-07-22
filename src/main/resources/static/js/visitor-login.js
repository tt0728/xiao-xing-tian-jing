document.addEventListener('DOMContentLoaded', function() {
    const visitorLoginForm = document.getElementById('visitorLoginForm');
    const loginMessage = document.getElementById('loginMessage');

    if (visitorLoginForm) {
        visitorLoginForm.addEventListener('submit', async function(event) {
            event.preventDefault();

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetch('/api/public/visitor-accounts/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password })
                });

                if (response.ok) {
                    // 登录成功，跳转到访客个人中心页面
                    window.location.href = 'visitor-dashboard.html';
                } else {
                    const errorData = await response.json();
                    loginMessage.textContent = '登录失败: ' + (errorData.message || '用户名或密码错误');
                    loginMessage.style.color = 'red';
                }
            } catch (error) {
                console.error('网络错误:', error);
                loginMessage.textContent = '登录失败: 无法连接到服务器。';
                loginMessage.style.color = 'red';
            }
        });
    }
});