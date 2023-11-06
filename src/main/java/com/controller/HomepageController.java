package com.controller;


import com.pojo.Cartoon;
import com.pojo.Result;
import com.pojo.WhiteHomepage;
import com.service.CartoonService;
import com.service.HomePageService;
import com.service.RoomService;
import com.utils.Code;
import com.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/homepage")
public class HomepageController {
    @Autowired
    private HttpServletRequest req;

    @Autowired
    private HomePageService homePageService;
    @Autowired
    private CartoonService cartoonService;

    @Autowired
    private RoomService roomService;

    //    绘本主页展示绘本内容，实际上就是去调用绘本的接口
    @GetMapping("/cartoon")
    public Result getCartoonPage() {
        String token = req.getHeader("token");
        Integer userId = JwtUtils.getId(token);
        Cartoon cartoon = cartoonService.getCartoon(userId);
        return cartoon != null ? Result.success(Code.CARTOON_GET_OK, cartoon)
                : Result.error(Code.CARTOON_GET_ERR, "获取失败");
    }

    //查看别人的绘本主页
    @GetMapping("/cartoon/{userId}")
    public Result getCartoonPage(@PathVariable Integer userId) {
        Cartoon cartoon = cartoonService.getCartoon(userId);
        return cartoon != null ? Result.success(Code.CARTOON_GET_OK, cartoon)
                : Result.error(Code.CARTOON_GET_ERR, "获取失败");
    }


    @GetMapping("/report")
    public Result getReportPage() {
        String token = req.getHeader("token");
        Integer userId = JwtUtils.getId(token);
        WhiteHomepage result = homePageService.getReportPage(userId);
        return result != null ? Result.success(Code.WHITEBOARD_GET_OK, result) :
                Result.error(Code.WHITEBOARD_GET_ERR, "主页内容获取失败");
    }

    @GetMapping("/report/{userId}")
    public Result getReportPage(@PathVariable Integer userId) {
        WhiteHomepage result = homePageService.getReportPage(userId);
        return result != null ? Result.success(Code.WHITEBOARD_GET_OK, result) :
                Result.error(Code.WHITEBOARD_GET_ERR, "主页内容获取失败");
    }

}