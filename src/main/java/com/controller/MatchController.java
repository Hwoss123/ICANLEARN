package com.controller;


import com.pojo.Cartoon;
import com.pojo.MatchDegree;
import com.pojo.Result;
import com.pojo.User;
import com.service.CartoonService;
import com.service.MatchService;
import com.utils.Code;
import com.utils.JwtUtils;
import com.utils.RedisConstant;
import com.utils.RedisUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/match")
public class MatchController {
    @Autowired
    private MatchService matchService;
    @Autowired
    public CartoonService cartoonService;
    @Autowired
    private HttpServletRequest req;

//    @Autowired
    private  RedisUtil redisUtil;

    @Autowired
    public MatchController(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    //获取匹配度表格
    @GetMapping("/degree")
    public Result getDegree() {
        String jwt = req.getHeader("token");
        //    根据id去获取对象的mbti，这里注意如果对象的mbti是null那么就会获取失败
        MatchDegree matchDegree = matchService.getDegree(jwt);
        if (matchDegree == null) {
            log.info("获取匹配度失败");
            return Result.error(Code.DEGREE_ERR, "获取匹配度失败");
        }
//        用fastjson会改变msg位置
//        return    JSONObject.toJSONString(Result.success(Code.DEGREE_OK,matchDegree), SerializerFeature.WriteMapNullValue);
        return Result.success(Code.DEGREE_OK, matchDegree);
    }

    //随机匹配
    @PostMapping()
    public Result match(@RequestBody(required = false) Map<String, List<String>> map, HttpSession session) {
//        这里需要判断是不是mbti的匹配，对应redis的key不一样
        if(map.containsKey("mbti")){
         return getResult(map, session, RedisConstant.MATCH_DEFINE_ID);
        }else {
            return getResult(map, session, RedisConstant.MATCH_RANDOM_ID);
        }
    }
        //获取实现需要返回的list
    private Result getResult(Map<String, List<String>> map, HttpSession session,String redisKey) {
        String jwt = req.getHeader("token");
        Claims claims = JwtUtils.parseJwt(jwt);
        Integer id = (Integer) claims.get("id");

//        这里需要进行两个redis实现
        String matchKey = redisKey + id;
        if (!map.containsKey("status")) {
            return Result.error("缺少请求状态码");
        }
        List<String> statusList = map.get("status");
//        这里如果是第一次点击就要1，其他都是0。因为要设置session
        if (statusList.get(0).equals("1")) {
            //如果是第一次进行操作
            List<User> resultList = matchService.matching(id, map);
            session.setAttribute("resultList", resultList);
            //这里返回的就是直接处理过后数据

            redisUtil.lSetList(matchKey, resultList, RedisConstant.MATCH_EXPIRE_TIME);
        }
        ///获取当前缓存的数据
        long size = redisUtil.lGetListSize(matchKey);

        List<User> userList;
        if (size < 5) {
            userList = redisUtil.lRemove(matchKey, size);
        } else {
            userList = redisUtil.lRemove(matchKey, 5);
        }
        if (size == 0) {
            log.info("get Users failed");
            return Result.error(Code.MATCH_USER_ERR, "获取用户异常");
        }

//        得到的userList要去获取Cartoon
        List<User> resultList = matchService.getCartoon(userList);
//        删除掉发送的数据
        return Result.success(Code.MATCH_USER_OK, resultList);
    }

    //随机匹配或者刷新
    @PostMapping("/{num}")
    public Result matchOne(@PathVariable Integer num) {
        String jwt = req.getHeader("token");
        Claims claims = JwtUtils.parseJwt(jwt);
        Integer id = (Integer) claims.get("id");
        if (num==1){
            return getResult(id,RedisConstant.MATCH_RANDOM_ID);
        }else {
            return getResult(id,RedisConstant.MATCH_DEFINE_ID);
        }

    }

    private Result getResult(Integer id,String RedisKey) {
        String matchKey = RedisKey + id;
//        这里看前端传的数据有多少个选择
        if (!redisUtil.hasKey(matchKey) || redisUtil.lGetListSize(matchKey) == 0) {
            return Result.error(Code.MATCH_USER_ERR, "获取用户异常");
        }

        List<Object> list = redisUtil.lRemove(matchKey, 1);
        User remove = (User) list.get(0);
        Cartoon cartoon = cartoonService.getCartoon(remove.getId());
        remove.setCartoon(cartoon);
        log.info("替换匹配用户:{}", remove);
//        删除掉发送的数据
        return Result.success(Code.MATCH_USER_OK, remove);
    }


    //清除匹配缓存
    @GetMapping()
    public Result matchByCartoon(HttpSession session) {
        // 清除上一次匹配的缓存
//        这里要做的是去重新放进session里面
        String jwt = req.getHeader("token");
        Integer id = JwtUtils.getId(jwt);
        String matchRandomKey = RedisConstant.MATCH_RANDOM_ID + id;
        String matchDefineKey = RedisConstant.MATCH_DEFINE_ID + id;
        List<User> userList = (List<User>) session.getAttribute("resultList");
        if (redisUtil.hasKey(matchRandomKey)) {
            redisUtil.lRemoveAll(matchRandomKey);
            redisUtil.lSetList(matchRandomKey, userList, RedisConstant.MATCH_EXPIRE_TIME);
        }else  if (redisUtil.hasKey(matchDefineKey)){
            redisUtil.lRemoveAll(matchDefineKey);
            redisUtil.lSetList(matchDefineKey, userList, RedisConstant.MATCH_EXPIRE_TIME);
        }
        log.info("清除匹配缓存");
        return Result.success();
    }

}