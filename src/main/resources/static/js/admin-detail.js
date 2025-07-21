document.addEventListener('DOMContentLoaded', fetchVisitorDetails);

async function fetchVisitorDetails() {
    const urlParams = new URLSearchParams(window.location.search);
    const visitorId = urlParams.get('id');
    const visitorDetailsDiv = document.getElementById('visitorDetails');
    const approvalMessageDiv = document.getElementById('approvalMessage');

    if (!visitorId) {
        visitorDetailsDiv.innerHTML = '<p style="color: red;">未找到访客ID。</p>';
        return;
    }

    try {
        const response = await fetch(`/api/admin/visitors/${visitorId}/details`);
        const visitor = await response.json();

        if (response.ok) {
            document.getElementById('detail-id').textContent = visitor.id;
            document.getElementById('detail-name').textContent = visitor.name;
            document.getElementById('detail-phone').textContent = visitor.phone;
            document.getElementById('detail-idCard').textContent = visitor.idCard || '无';
            document.getElementById('detail-workUnit').textContent = visitor.workUnit || '无';
            document.getElementById('detail-visitDate').textContent = visitor.visitDate;
            document.getElementById('detail-visitTime').textContent = `${visitor.visitTimeStart || '未指定'} - ${visitor.visitTimeEnd || '未指定'}`;
            document.getElementById('detail-purpose').textContent = visitor.purpose || '无';
            document.getElementById('detail-visitedPerson').textContent = visitor.visitedPerson;
            document.getElementById('detail-visitedPersonDepartment').textContent = visitor.visitedPersonDepartment || '无';
            document.getElementById('detail-status').textContent = visitor.status;

            const companionsList = document.getElementById('companionsList');
            companionsList.innerHTML = ''; // 清空现有内容
            if (visitor.companions && visitor.companions.length > 0) {
                visitor.companions.forEach(comp => {
                    const li = document.createElement('li');
                    li.textContent = `姓名: ${comp.name}, 电话: ${comp.phone || '无'}, 证件: ${comp.idCard || '无'}`;
                    companionsList.appendChild(li);
                });
            } else {
                companionsList.innerHTML = '<li>无同行人</li>';
            }

            // 根据当前状态，禁用审批按钮
            const currentStatus = visitor.status.toUpperCase();
            if (currentStatus === 'APPROVED' || currentStatus === 'REJECTED' || currentStatus === 'IN_VISIT' || currentStatus === 'DEPARTED') {
                document.getElementById('approvalActions').innerHTML = `<p>该预约已处于 <strong>${currentStatus}</strong> 状态，无法再次审批。</p>`;
            }

        } else {
            visitorDetailsDiv.innerHTML = `<p style="color: red;">加载访客详情失败: ${visitor.message || '未知错误'}</p>`;
            console.error('加载访客详情失败:', visitor);
        }
    } catch (error) {
        visitorDetailsDiv.innerHTML = '<p style="color: red;">网络错误或服务器无响应。</p>';
        console.error('获取访客详情时发生错误:', error);
    }
}

async function updateVisitorStatus(status) {
    const urlParams = new URLSearchParams(window.location.search);
    const visitorId = urlParams.get('id');
    const approvalMessageDiv = document.getElementById('approvalMessage');
    approvalMessageDiv.textContent = '';
    approvalMessageDiv.className = '';

    if (!visitorId) {
        approvalMessageDiv.textContent = '无法获取访客ID。';
        approvalMessageDiv.classList.add('error');
        return;
    }

    try {
        const response = await fetch(`/api/admin/visitors/${visitorId}/review`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ status: status })
        });

        const result = await response.json();

        if (response.ok) {
            approvalMessageDiv.textContent = `审批成功，状态已更新为: ${result.status}`;
            approvalMessageDiv.classList.add('success');
            // 更新页面显示的状态
            document.getElementById('detail-status').textContent = result.status;
            // 禁用审批按钮
            document.getElementById('approvalActions').innerHTML = `<p>该预约已处于 <strong>${result.status}</strong> 状态，无法再次审批。</p>`;

        } else {
            approvalMessageDiv.textContent = '审批失败: ' + (result.message || '未知错误');
            approvalMessageDiv.classList.add('error');
            console.error('审批失败:', result);
        }
    } catch (error) {
        approvalMessageDiv.textContent = '网络错误或服务器无响应。';
        approvalMessageDiv.classList.add('error');
        console.error('审批时发生错误:', error);
    }
}

window.approveVisitor = function() {
    updateVisitorStatus('APPROVED');
};

window.rejectVisitor = function() {
    updateVisitorStatus('REJECTED');
};