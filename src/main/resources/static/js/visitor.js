document.addEventListener('DOMContentLoaded', () => {
    const visitorForm = document.getElementById('visitorForm');
    const messageDiv = document.getElementById('message');
    const companionsContainer = document.getElementById('companionsContainer');
    let companionCount = 0; // 用于追踪同行人数量

    // 获取当前日期并格式化为 YYYY-MM-DD
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    const minDate = `${year}-${month}-${day}`;

    // 设置日期输入框的最小日期
    const visitDateInput = document.querySelector('input[name="visitDate"]');
    if (visitDateInput) {
        visitDateInput.min = minDate;
    }

    // 添加同行人表单块
    window.addCompanion = function() {
        companionCount++;
        const companionDiv = document.createElement('div');
        companionDiv.classList.add('companion-item');
        companionDiv.innerHTML = `
            <h3>同行人 ${companionCount}</h3>
            <label for="companionName${companionCount}">姓名:</label>
            <input type="text" id="companionName${companionCount}" name="companionName${companionCount}"><br>
            <label for="companionPhone${companionCount}">电话:</label>
            <input type="tel" id="companionPhone${companionCount}" name="companionPhone${companionCount}" pattern="[0-9]{10,11}"><br>
            <label for="companionIdCard${companionCount}">证件号码:</label>
            <input type="text" id="companionIdCard${companionCount}" name="companionIdCard${companionCount}" pattern="[0-9]{18}|[0-9]{17}[xX]"><br>
            <button type="button" onclick="removeCompanion(this)">移除</button><br>
        `;
        companionsContainer.appendChild(companionDiv);
    };

    // 移除同行人表单块
    window.removeCompanion = function(buttonElement) {
        buttonElement.closest('.companion-item').remove();
    };

    visitorForm.addEventListener('submit', async (event) => {
        event.preventDefault(); // 阻止表单默认提交行为

        messageDiv.textContent = ''; // 清除之前的消息
        messageDiv.className = '';

        const formData = new FormData(visitorForm);
        const visitorData = {};
        formData.forEach((value, key) => {
            // 排除同行人字段，单独处理
            if (!key.startsWith('companion')) {
                visitorData[key] = value;
            }
        });

        // 处理同行人数据
        const companions = [];
        document.querySelectorAll('.companion-item').forEach((item, index) => {
            const companionName = item.querySelector(`[name^="companionName"]`).value;
            const companionPhone = item.querySelector(`[name^="companionPhone"]`).value;
            const companionIdCard = item.querySelector(`[name^="companionIdCard"]`).value;

            if (companionName || companionPhone || companionIdCard) { // 只要有一个字段不为空就认为有同行人
                companions.push({
                    name: companionName,
                    phone: companionPhone,
                    idCard: companionIdCard
                });
            }
        });
        visitorData.companions = companions;

        try {
            // 确保日期和时间格式正确
            if (visitorData.visitDate) {
                visitorData.visitDate = visitorData.visitDate; // Date input type already gives YYYY-MM-DD
            }
            if (visitorData.visitTimeStart) {
                visitorData.visitTimeStart = visitorData.visitTimeStart + ":00"; // Time input type gives HH:MM, need HH:MM:SS
            }
            if (visitorData.visitTimeEnd) {
                visitorData.visitTimeEnd = visitorData.visitTimeEnd + ":00"; // Time input type gives HH:MM, need HH:MM:SS
            }

            const response = await fetch('/api/public/visitors/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(visitorData)
            });

            const result = await response.json();

            if (response.ok) {
                messageDiv.textContent = '预约提交成功！您的预约ID是：' + result.id + '，请等待管理员审核。';
                messageDiv.classList.add('success');
                visitorForm.reset(); // 清空表单
                companionsContainer.innerHTML = ''; // 清空同行人
                companionCount = 0;
                // 可以在这里显示二维码或者预约凭证信息
            } else {
                messageDiv.textContent = '预约提交失败: ' + (result.message || '未知错误');
                messageDiv.classList.add('error');
                console.error('预约提交失败:', result);
            }
        } catch (error) {
            messageDiv.textContent = '网络错误或服务器无响应。';
            messageDiv.classList.add('error');
            console.error('提交预约时发生错误:', error);
        }
    });
});