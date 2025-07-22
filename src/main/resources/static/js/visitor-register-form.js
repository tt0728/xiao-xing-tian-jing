document.addEventListener('DOMContentLoaded', function() {
    const visitorRegisterForm = document.getElementById('visitorRegisterForm');
    const registerMessage = document.getElementById('registerMessage');

    if (visitorRegisterForm) {
        visitorRegisterForm.addEventListener('submit', async function(event) {
            event.preventDefault();

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetch('/api/public/visitor-accounts/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password })
                });

                if (response.ok) {
                    registerMessage.textContent = '注册成功！请登录。';
                    registerMessage.style.color = 'green';
                    visitorRegisterForm.reset();
                    // 注册成功后可以考虑自动跳转到登录页
                    setTimeout(() => {
                        window.location.href = 'visitor-login.html';
                    }, 2000);
                } else {
                    const errorData = await response.json();
                    registerMessage.textContent = '注册失败: ' + (errorData.message || '未知错误');
                    registerMessage.style.color = 'red';
                }
            } catch (error) {
                console.error('网络错误:', error);
                registerMessage.textContent = '注册失败: 无法连接到服务器。';
                registerMessage.style.color = 'red';
            }
        });
    }
});