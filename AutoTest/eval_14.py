import subprocess
import json

def SendPrivateMessageCheck(receiverUsername, senderId, messageContent='催更'):
    """
    检查用户是否给关注的第一个博主发送了私信
    任务14: 从"我"->"关注"进入第一个关注的博主主页，点击"私信"按钮发送"催更"
    """
    # 从设备获取消息记录
    messages_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/messages.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 从设备获取关注列表
    follows_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/follows.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if messages_result.returncode != 0 or follows_result.returncode != 0:
        return False

    # 解析 JSON
    try:
        messages_data = json.loads(messages_result.stdout) if messages_result.stdout else []
        follows_data = json.loads(follows_result.stdout) if follows_result.stdout else []
    except (json.JSONDecodeError, TypeError):
        return False

    # 检查私信发送
    try:
        # 获取用户的关注列表，找到第一个关注的博主
        user_follows = [f for f in follows_data if f.get('followerId') == senderId]
        if not user_follows:
            return False

        user_follows.sort(key=lambda x: x.get('followedAt', ''))
        first_following = user_follows[0].get('following', {})
        receiver_id = first_following.get('id')

        # 检查是否有发送给该博主的最新消息
        for message in reversed(messages_data):
            if (message.get('sender', {}).get('id') == senderId and
                message.get('receiver', {}).get('id') == receiver_id and
                message.get('content') == messageContent and
                message.get('type') == 'TEXT'):
                return True

        return False

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(SendPrivateMessageCheck(
        receiverUsername='',  # 可以留空，脚本会自动从关注列表获取
        senderId='user_current',
        messageContent='催更'
    ))
