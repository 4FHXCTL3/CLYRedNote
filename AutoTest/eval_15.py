import subprocess
import json
import sys
import io

# 设置 UTF-8 编码以支持 emoji 输出
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def LikeCommentCheck(userId):
    """
    检查用户是否对首页第二篇笔记的第一条评论进行了点赞
    任务15: 在首页进入第二篇笔记的详情页点击查看第一条评论，对评论进行"点赞"
    """
    # 从设备获取点赞记录
    result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/likes.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if result.returncode != 0 or not result.stdout:
        print(f"❌ Failed to read likes file")
        print(f"   Reason: ADB command failed (return code: {result.returncode})")
        if result.stderr:
            print(f"   Error: {result.stderr}")
        return False

    # 解析 JSON
    try:
        data = json.loads(result.stdout)
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse likes data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查评论点赞
    try:
        if not data or len(data) == 0:
            print(f"❌ Likes list is empty")
            print(f"   Reason: No like records found")
            print(f"   Expected: At least one comment like for user '{userId}'")
            return False

        # 查找用户对评论的点赞记录
        comment_likes = [like for like in data
                        if like.get('userId') == userId
                        and like.get('targetType') == 'COMMENT']

        # 如果有最新的评论点赞记录，返回 True
        if comment_likes:
            # 按时间排序，获取最新的点赞
            latest_like = sorted(comment_likes, key=lambda x: x.get('likedAt', ''), reverse=True)[0]
            target_id = latest_like.get('targetId', 'Unknown')
            liked_at = latest_like.get('likedAt', 'Unknown')
            print(f"✓ Successfully liked a comment")
            print(f"   Comment ID: {target_id}")
            print(f"   Liked at: {liked_at}")
            return True

        # Check if user liked notes instead of comments
        note_likes = [like for like in data
                     if like.get('userId') == userId
                     and like.get('targetType') == 'NOTE']

        print(f"❌ No comment like records for user")
        print(f"   Reason: User '{userId}' has not liked any comments")
        print(f"   Expected: At least one comment like (targetType: COMMENT)")

        if note_likes:
            print(f"   Note: User has liked {len(note_likes)} note(s), but no comments")

        # Show total likes in system
        total_comment_likes = [like for like in data if like.get('targetType') == 'COMMENT']
        print(f"   Total comment likes in system: {len(total_comment_likes)}")

        return False

    except Exception as e:
        print(f"❌ Error while checking comment like")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(LikeCommentCheck(
        userId='user_current'
    ))
