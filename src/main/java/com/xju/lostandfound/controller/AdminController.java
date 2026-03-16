//package com.xju.lostandfound.controller;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.xju.lostandfound.common.result.Result;
//import com.xju.lostandfound.common.utils.JwtUtils;
//import com.xju.lostandfound.entity.FoundItem;
//import com.xju.lostandfound.entity.LostItem;
//import com.xju.lostandfound.entity.User;
//import com.xju.lostandfound.service.FoundItemService;
//import com.xju.lostandfound.service.LostItemService;
//import com.xju.lostandfound.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/admin")
//public class AdminController {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private LostItemService lostItemService;
//
//    @Autowired
//    private FoundItemService foundItemService;
//
//    /**
//     * 检查当前用户是否为管理员
//     */
//    private boolean isAdmin(String token) {
//        if (token == null || token.trim().isEmpty()) return false;
//        String userIdStr = JwtUtils.getClaimsByToken(token);
//        if (userIdStr == null) return false;
//        Long userId = Long.parseLong(userIdStr);
//        User user = userService.getById(userId);
//        return user != null && user.getRole() == 1; // 假设 role 1 为管理员
//    }
//
//    // ================== 用户管理 ==================
//
//    @GetMapping("/users")
//    public Result<Page<User>> listUsers(@RequestParam(defaultValue = "1") Integer current,
//                                        @RequestParam(defaultValue = "10") Integer size,
//                                        @RequestParam(required = false) String keyword,
//                                        @RequestHeader("token") String token) {
//        if (!isAdmin(token)) return Result.error(403, "无权访问");
//
//        Page<User> page = new Page<>(current, size);
//        QueryWrapper<User> wrapper = new QueryWrapper<>();
//        if (keyword != null && !keyword.trim().isEmpty()) {
//            wrapper.and(w -> w.like("username", keyword)
//                    .or().like("nickname", keyword)
//                    .or().like("real_name", keyword)
//                    .or().like("mobile", keyword));
//        }
//        wrapper.orderByDesc("create_time");
//        Page<User> result = userService.page(page, wrapper);
//        // 隐藏密码
//        result.getRecords().forEach(u -> u.setPassword(null));
//        return Result.success(result);
//    }
//
//    @PutMapping("/users/{id}/status")
//    public Result<String> updateUserStatus(@PathVariable Long id,
//                                           @RequestParam Integer status,
//                                           @RequestHeader("token") String token) {
//        if (!isAdmin(token)) return Result.error(403, "无权访问");
//        User user = userService.getById(id);
//        if (user == null) return Result.error("用户不存在");
//        user.setStatus(status);
//        userService.updateById(user);
//        return Result.success("用户状态更新成功");
//    }
//
//    @DeleteMapping("/users/{id}")
//    public Result<String> deleteUser(@PathVariable Long id,
//                                     @RequestHeader("token") String token) {
//        if (!isAdmin(token)) return Result.error(403, "无权访问");
//        // 这里可能需要级联删除该用户发布的物品、匹配记录等，简单起见先只删用户
//        userService.removeById(id);
//        return Result.success("用户删除成功");
//    }
//
//    @GetMapping("/users/{id}")
//    public Result<User> getUserDetail(@PathVariable Long id,
//                                      @RequestHeader("token") String token) {
//        if (!isAdmin(token)) return Result.error(403, "无权访问");
//        User user = userService.getById(id);
//        if (user == null) return Result.error("用户不存在");
//        user.setPassword(null);
//        return Result.success(user);
//    }
//
//    // ================== 失物管理 ==================
//
//    @GetMapping("/items/lost")
//    public Result<Page<LostItem>> listLostItems(@RequestParam(defaultValue = "1") Integer current,
//                                                @RequestParam(defaultValue = "10") Integer size,
//                                                @RequestParam(required = false) String keyword,
//                                                @RequestParam(required = false) Integer status, // 0未解决 1已解决
//                                                @RequestHeader("token") String token) {
//        if (!isAdmin(token)) return Result.error(403, "无权访问");
//
//        Page<LostItem> page = new Page<>(current, size);
//        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();
//        if (keyword != null && !keyword.trim().isEmpty()) {
//            wrapper.and(w -> w.like("item_name", keyword)
//                    .or().like("description", keyword));
//        }
//        if (status != null) {
//            wrapper.eq("status", status);
//        }
//        wrapper.orderByDesc("create_time");
//        Page<LostItem> result = lostItemService.page(page, wrapper);
//        // 清空敏感特征
//        result.getRecords().forEach(item -> {
//            item.setOcrText(null);
//            item.setImageFeature(null);
//        });
//        return Result.success(result);
//    }
//
//    @GetMapping("/items/found")
//    public Result<Page<FoundItem>> listFoundItems(@RequestParam(defaultValue = "1") Integer current,
//                                                  @RequestParam(defaultValue = "10") Integer size,
//                                                  @RequestParam(required = false) String keyword,
//                                                  @RequestParam(required = false) Integer status,
//                                                  @RequestHeader("token") String token) {
//        if (!isAdmin(token)) return Result.error(403, "无权访问");
//
//        Page<FoundItem> page = new Page<>(current, size);
//        QueryWrapper<FoundItem> wrapper = new QueryWrapper<>();
//        if (keyword != null && !keyword.trim().isEmpty()) {
//            wrapper.and(w -> w.like("item_name", keyword)
//                    .or().like("description", keyword));
//        }
//        if (status != null) {
//            wrapper.eq("status", status);
//        }
//        wrapper.orderByDesc("create_time");
//        Page<FoundItem> result = foundItemService.page(page, wrapper);
//        result.getRecords().forEach(item -> {
//            item.setOcrText(null);
//            item.setImageFeature(null);
//        });
//        return Result.success(result);
//    }
//
//    @PutMapping("/items/lost/{id}/status")
//    public Result<String> updateLostItemStatus(@PathVariable Long id,
//                                               @RequestParam Integer status,
//                                               @RequestHeader("token") String token) {
//        if (!isAdmin(token)) return Result.error(403, "无权访问");
//        LostItem item = lostItemService.getById(id);
//        if (item == null) return Result.error("物品不存在");
//        item.setStatus(status);
//        lostItemService.updateById(item);
//        return Result.success("物品状态更新成功");
//    }
//
//    @PutMapping("/items/found/{id}/status")
//    public Result<String> updateFoundItemStatus(@PathVariable Long id,
//                                                @RequestParam Integer status,
//                                                @RequestHeader("token") String token) {
//        if (!isAdmin(token)) return Result.error(403, "无权访问");
//        FoundItem item = foundItemService.getById(id);
//        if (item == null) return Result.error("物品不存在");
//        item.setStatus(status);
//        foundItemService.updateById(item);
//        return Result.success("物品状态更新成功");
//    }
//
//    @DeleteMapping("/items/lost/{id}")
//    public Result<String> deleteLostItem(@PathVariable Long id,
//                                         @RequestHeader("token") String token) {
//        if (!isAdmin(token)) return Result.error(403, "无权访问");
//        lostItemService.removeById(id);
//        return Result.success("删除成功");
//    }
//
//    @DeleteMapping("/items/found/{id}")
//    public Result<String> deleteFoundItem(@PathVariable Long id,
//                                          @RequestHeader("token") String token) {
//        if (!isAdmin(token)) return Result.error(403, "无权访问");
//        foundItemService.removeById(id);
//        return Result.success("删除成功");
//    }
//}
package com.xju.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xju.lostandfound.common.result.Result;
import com.xju.lostandfound.common.utils.JwtUtils;
import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.entity.User;
import com.xju.lostandfound.service.FoundItemService;
import com.xju.lostandfound.service.LostItemService;
import com.xju.lostandfound.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private LostItemService lostItemService;
    @Autowired
    private FoundItemService foundItemService;

    /** 检查当前登录用户是否为管理员 */
    private boolean isAdmin(String token) {
        if (token == null || token.trim().isEmpty()) return false;
        String userIdStr = JwtUtils.getClaimsByToken(token);
        if (userIdStr == null) return false;
        Long userId = Long.parseLong(userIdStr);
        User user = userService.getById(userId);
        return user != null && user.getRole() == 0; // role = 0 代表管理员，不是1！！！！！！！
    }

    // ================== 用户管理 ==================

    /** 分页查询用户列表（支持关键词搜索） */
    @GetMapping("/users")
    public Result<Page<User>> listUsers(@RequestParam(defaultValue = "1") Integer current,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestHeader("token") String token) {
        if (!isAdmin(token)) return Result.error(403, "无权访问");
        Page<User> page = new Page<>(current, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("username", keyword)
                    .or().like("nickname", keyword)
                    .or().like("real_name", keyword)
                    .or().like("mobile", keyword));
        }
        wrapper.orderByDesc("create_time");
        Page<User> result = userService.page(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null)); // 隐藏密码
        return Result.success(result);
    }

    /** 启用/禁用用户 */
    @PutMapping("/users/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Long id,
                                           @RequestParam Integer status,
                                           @RequestHeader("token") String token) {
        if (!isAdmin(token)) return Result.error(403, "无权访问");
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        user.setStatus(status);
        userService.updateById(user);
        return Result.success("用户状态更新成功");
    }

    /** 删除用户（谨慎操作） */
    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id,
                                     @RequestHeader("token") String token) {
        if (!isAdmin(token)) return Result.error(403, "无权访问");
        userService.removeById(id);
        return Result.success("用户删除成功");
    }

    /** 查看用户详情 */
    @GetMapping("/users/{id}")
    public Result<User> getUserDetail(@PathVariable Long id,
                                      @RequestHeader("token") String token) {
        if (!isAdmin(token)) return Result.error(403, "无权访问");
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        user.setPassword(null);
        return Result.success(user);
    }

    // ================== 失物管理 ==================

    /** 分页查询失物列表（支持关键词、状态筛选） */
    @GetMapping("/items/lost")
    public Result<Page<LostItem>> listLostItems(@RequestParam(defaultValue = "1") Integer current,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer status,
                                                @RequestHeader("token") String token) {
        if (!isAdmin(token)) return Result.error(403, "无权访问");
        Page<LostItem> page = new Page<>(current, size);
        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("item_name", keyword).or().like("description", keyword));
        }
        if (status != null) wrapper.eq("status", status);
        wrapper.orderByDesc("create_time");
        Page<LostItem> result = lostItemService.page(page, wrapper);
        result.getRecords().forEach(item -> { item.setOcrText(null); item.setImageFeature(null); });
        return Result.success(result);
    }

    /** 分页查询招领列表（支持关键词、状态筛选） */
    @GetMapping("/items/found")
    public Result<Page<FoundItem>> listFoundItems(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestHeader("token") String token) {
        if (!isAdmin(token)) return Result.error(403, "无权访问");
        Page<FoundItem> page = new Page<>(current, size);
        QueryWrapper<FoundItem> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("item_name", keyword).or().like("description", keyword));
        }
        if (status != null) wrapper.eq("status", status);
        wrapper.orderByDesc("create_time");
        Page<FoundItem> result = foundItemService.page(page, wrapper);
        result.getRecords().forEach(item -> { item.setOcrText(null); item.setImageFeature(null); });
        return Result.success(result);
    }

    /** 修改失物状态 */
    @PutMapping("/items/lost/{id}/status")
    public Result<String> updateLostItemStatus(@PathVariable Long id,
                                               @RequestParam Integer status,
                                               @RequestHeader("token") String token) {
        if (!isAdmin(token)) return Result.error(403, "无权访问");
        LostItem item = lostItemService.getById(id);
        if (item == null) return Result.error("物品不存在");
        item.setStatus(status);
        lostItemService.updateById(item);
        return Result.success("物品状态更新成功");
    }

    /** 修改招领状态 */
    @PutMapping("/items/found/{id}/status")
    public Result<String> updateFoundItemStatus(@PathVariable Long id,
                                                @RequestParam Integer status,
                                                @RequestHeader("token") String token) {
        if (!isAdmin(token)) return Result.error(403, "无权访问");
        FoundItem item = foundItemService.getById(id);
        if (item == null) return Result.error("物品不存在");
        item.setStatus(status);
        foundItemService.updateById(item);
        return Result.success("物品状态更新成功");
    }

    /** 删除失物记录 */
    @DeleteMapping("/items/lost/{id}")
    public Result<String> deleteLostItem(@PathVariable Long id,
                                         @RequestHeader("token") String token) {
        if (!isAdmin(token)) return Result.error(403, "无权访问");
        lostItemService.removeById(id);
        return Result.success("删除成功");
    }

    /** 删除招领记录 */
    @DeleteMapping("/items/found/{id}")
    public Result<String> deleteFoundItem(@PathVariable Long id,
                                          @RequestHeader("token") String token) {
        if (!isAdmin(token)) return Result.error(403, "无权访问");
        foundItemService.removeById(id);
        return Result.success("删除成功");
    }
}