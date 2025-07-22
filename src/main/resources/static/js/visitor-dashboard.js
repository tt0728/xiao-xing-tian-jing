document.addEventListener('DOMContentLoaded', async function() {
    const appointmentsList = document.getElementById('appointmentsList');
    const messageElement = document.getElementById('message');
    const welcomeUsername = document.getElementById('welcomeUsername');
    const logoutButton = document.getElementById('logoutButton');

    welcomeUsername.textContent = "访客"; // 默认值

    // 登出按钮事件
    if (logoutButton) {
        logoutButton.addEventListener('click', async function() {
            try {
                const response = await fetch('/api/public/visitor-accounts/logout', {
                    method: 'POST'
                });
                if (response.ok) {
                    alert('您已成功登出。');
                    window.location.href = 'index.html'; // 返回首页
                } else {
                    alert('登出失败，请重试。');
                }
            } catch (error) {
                console.error('登出网络错误:', error);
                alert('登出失败: 无法连接到服务器。');
            }
        });
    }


    async function fetchAppointments() {
        try {
            const response = await fetch('/api/public/visitors/my-appointments'); // 调用新的API
            if (response.ok) {
                const appointments = await response.json();
                appointmentsList.innerHTML = ''; // 清空现有内容
                if (appointments && appointments.length > 0) {
                    appointments.forEach(app => {
                        const itemDiv = document.createElement('div');
                        itemDiv.className = 'appointment-item';
                        itemDiv.innerHTML = `
                            <p><strong>预约ID:</strong> ${app.id}</p>
                            <p><strong>姓名:</strong> ${app.name}</p>
                            <p><strong>手机号:</strong> ${app.phone}</p>
                            <p><strong>拜访日期:</strong> ${app.visitDate}</p>
                            <p><strong>拜访时间:</strong> ${app.visitTime}</p>
                            <p><strong>目的:</strong> ${app.purpose}</p>
                            <p><strong>状态:</strong> <span class="status-${app.status}">${app.status}</span></p>
                            ${app.comments ? `<p><strong>审批意见:</strong> ${app.comments}</p>` : ''}
                            ${app.entryTime ? `<p><strong>入场时间:</strong> ${app.entryTime}</p>` : ''}
                            ${app.exitTime ? `<p><strong>离场时间:</strong> ${app.exitTime}</p>` : ''}
                        `;
                        appointmentsList.appendChild(itemDiv);
                    });
                } else {
                    appointmentsList.innerHTML = '<p>您还没有任何预约记录。</p>';
                }
            } else if (response.status === 401) {
                messageElement.textContent = '您尚未登录，请先登录。';
                messageElement.style.color = 'red';
                appointmentsList.innerHTML = '';
                // 未登录，自动跳转到登录页
                setTimeout(() => {
                    window.location.href = 'visitor-login.html';
                }, 1500);
            } else {
                messageElement.textContent = '获取预约列表失败。';
                messageElement.style.color = 'red';
            }
        } catch (error) {
            console.error('网络错误:', error);
            messageElement.textContent = '加载预约列表失败: 无法连接到服务器。';
            messageElement.style.color = 'red';
        }
    }

    fetchAppointments(); // 页面加载时立即获取预约列表
});