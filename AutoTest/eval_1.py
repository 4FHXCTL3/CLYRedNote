import subprocess
import json

def MessageSendCheck(receiverId, senderId, message_content):
    # 从设备获取文件
    # run里面的com.example.test05是APP名字；files/messages.json是文件路径
    subprocess.run(['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/messages.json'],
                    stdout=open('messages.json', 'w'))

    # 读取文件
    with open('messages.json', 'r', encoding='utf-8') as f:
        data = json.load(f)

    # 判断给定的信息是否存在于聊天记录里面
    try:
        # 遍历消息列表，查找最新的匹配消息
        for message in reversed(data):  # 从最新消息开始检查
            # 获取sender和receiver的id
            sender_id = message.get('sender', {}).get('id')
            receiver_id = message.get('receiver', {}).get('id')
            
            if (sender_id == senderId and 
                receiver_id == receiverId and
                message.get('content') == message_content and
                message.get('type') == 'TEXT'):
                return True
        return False
    except:
        return False

if __name__ == "__main__":
    print(MessageSendCheck(
         receiverId='user_001',     # 小红薯美妆达人的id是user_001
         senderId='user_current',   # 这是"我"
         message_content='你好'      # 发送内容
    ))