package com.xju.lostandfound.controller;
import com.xju.lostandfound.common.utils.JwtUtils;
import com.xju.lostandfound.common.result.Result;
import com.xju.lostandfound.entity.User;
import com.xju.lostandfound.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        String token = userService.login(user.getUsername(), user.getPassword());
        if (token != null) {
            return Result.success(token);
        }
        return Result.error("账号或密码错误");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        boolean success = userService.register(user);
        return success ? Result.success("注册成功") : Result.error("该学号已被注册");
    }



    /**
     * 获取当前登录用户信息
     * GET /user/info
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader(value = "token", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请先登录");
        }
        String userIdStr = JwtUtils.getClaimsByToken(token);
        if (userIdStr == null) {
            return Result.error(401, "Token无效或已过期");
        }

        // 提取真实用户 ID 并查询数据库
        Long currentUserId = Long.parseLong(userIdStr);
        User user = userService.getById(currentUserId);

        if (user != null) {
            user.setPassword(null); // 💡 安全起见，传给前端前把密码清空
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }
    /**
     * 修改个人信息
     * POST /user/update
     */
    @PostMapping("/update")
    public Result<String> updateUserInfo(@RequestBody User user, @RequestHeader(value = "token", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请先登录");
        }
        String userIdStr = JwtUtils.getClaimsByToken(token);
        if (userIdStr == null) {
            return Result.error(401, "Token无效或已过期");
        }

        // 1. 获取当前登录用户的真实 ID
        Long currentUserId = Long.parseLong(userIdStr);

        // 2. 强制将要更新的 ID 设为当前用户 ID (防止黑客篡改别人信息)
        user.setId(currentUserId);

        // 3. 安全防护：把不允许用户自己修改的敏感字段设为 null，防止被恶意覆盖
        user.setUsername(null);     // 学号不允许改
        user.setPassword(null);     // 密码修改应该单独做接口，这里不处理
        user.setCreditScore(null);  // 信用分只能系统改
        user.setRole(null);         // 权限不能自己改
        user.setStatus(null);       // 状态不能自己改

        // 4. 执行更新
        boolean success = userService.updateById(user);

        if (success) {
            return Result.success("个人信息更新成功！");
        } else {
            return Result.error("更新失败，请重试");
        }
    }
}
