import subprocess
import json

def MessageSendCheck(receiverId, senderId, message_content):
    # 从设备获取文件
    # run里面的com.example.fakewechat是APP名字；files/messages.json是文件路径
    subprocess.run(['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/messages.json'],
                    stdout=open('messages.json', 'w'))

    # 读取文件
    with open('messages.json', 'r', encoding='utf-8') as f:
        data = json.load(f)

    # 判断给定的信息是否存在于聊天记录里面
    try:
        item = data['privateChatMessages'][receiverId][-1]
        if item['senderId'] == senderId and item['content'] == message_content:
            return True
        else:
            return False
    except:
            return False

if __name__ == "__main__":
    print(MessageSendCheck(
         receiverId='user_002',     # 我的微信里面，北大李老师的id是user_2
         senderId='current_user', # 这是“我”
         message_content='你好'  #  发送内容
    ))