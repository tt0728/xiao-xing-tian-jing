document.addEventListener('DOMContentLoaded', function() {
    const visitDateInput = document.getElementById('visitDate');

    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    const minDate = `${year}-${month}-${day}`;

    if (visitDateInput) {
        visitDateInput.min = minDate;
        visitDateInput.value = minDate;
    }

    const visitorForm = document.getElementById('visitorForm');
    const messageElement = document.getElementById('message');

    if (visitorForm) {
        visitorForm.addEventListener('submit', async function(event) {
            event.preventDefault();

    const visitorData = {
                name: document.getElementById('name').value,
                phone: document.getElementById('phone').value,
                idCard: document.getElementById('idCard').value,
                visitedPerson: document.getElementById('visitedPerson').value,
                visitedPersonDepartment: document.getElementById('visitedPersonDepartment').value,
                visitDate: document.getElementById('visitDate').value,
                visitTimeStart: document.getElementById('visitTimeStart').value,
                visitTimeEnd: document.getElementById('visitTimeEnd').value,
                purpose: document.getElementById('purpose').value,
                companionsContainer: []
            };

            const companionDivs = document.querySelectorAll('#companionsContainer .companion-item');
            companionDivs.forEach(div => {
                const companion = {
                    name: div.querySelector('.companion-name').value,
                    phone: div.querySelector('.companion-phone').value,
                    idCard: div.querySelector('.companion-idCard').value
                };
                visitorData.companionsContainer.push(companion);
            });

            try {
                const response = await fetch('/api/public/visitors/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(visitorData)
                });

                if (response.ok) {
                    const data = await response.json();
                    messageElement.textContent = '预约成功！您的预约ID是：' + data.id + '，请等待审核。';
                    messageElement.style.color = 'green';
                    visitorForm.reset();
                    if (visitDateInput) {
                         visitDateInput.min = minDate;
                         visitDateInput.value = minDate;
                    }
                    document.getElementById('companionsContainer').innerHTML = ''; // 清空同行人
                } else if (response.status === 401) {
                    messageElement.textContent = '您尚未登录，请先登录才能提交预约。';
                    messageElement.style.color = 'red';
                    setTimeout(() => {
                        window.location.href = 'visitor-login.html'; // 引导用户登录
                    }, 1500);
                } else {
                    const errorData = await response.json();
                    messageElement.textContent = '预约失败: ' + (errorData.message || '未知错误');
                    messageElement.style.color = 'red';
                }
            } catch (error) {
                console.error('网络错误:', error);
                messageElement.textContent = '预约失败: 无法连接到服务器。';
                messageElement.style.color = 'red';
            }
        });
    }
});

// 添加同行人表单项的函数
function addCompanion() {
    const companionsContainer = document.getElementById('companionsContainer');
    if (!companionsContainer) {
        console.error("companionsContainer 元素不存在！");
        return;
    }

    const companionCount = companionsContainer.children.length + 1;

    const companionItem = document.createElement('div');
    companionItem.className = 'companion-item';
    companionItem.innerHTML = `
        <h3>同行人 ${companionCount}</h3>
        <label for="companionName${companionCount}">姓名:</label>
        <input type="text" class="companion-name" id="companionName${companionCount}" required><br>

        <label for="companionPhone${companionCount}">手机号:</label>
        <input type="tel" class="companion-phone" id="companionPhone${companionCount}" pattern="[0-9]{10,12}"><br>

        <label for="companionIdCard${companionCount}">身份证号:</label>
        <input type="text" class="companion-idCard" id="companionIdCard${companionCount}" pattern="[0-9X]{18}"><br>
        <button type="button" onclick="removeCompanion(this)">移除</button><br>
    `;
    companionsContainer.appendChild(companionItem);
}


function removeCompanion(button) {
    const item = button.parentElement;
    item.remove();
}
