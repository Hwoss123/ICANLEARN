package com.mapper;


import com.pojo.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OccupationExplodeMapper {
//    通过id去获取职业信息
    @Select("select * from occupation_explode where  id = #{id}")
     OccupationExplode getOccupationById(Integer id);

//    用like去获取，后面改成es
    @Select("select * from occupation_explode where job like CONCAT('%', #{keyword}, '%')")
    List<OccupationExplode> getOccupation(String keyword);

//    添加到历史记录
    @Insert("insert into search_history  (user_id, content) values(#{userId}, #{content})")
     int addHistory(Integer userId ,String content);

//    获取之前的历史的记录
    @Select("select * from search_history where user_id = #{userId}")
    List<SearchHistory> historyList(Integer userId);
//    获取所有的职业
    @Select("SELECT * from occupation_explode")
    List<OccupationExplode> getOccupations();

//    增加某个职业信息的点赞
    @Update("update occupation_explode set likes = likes + 1 where id = #{id}")
    void addLike(Integer id);

    //    减少某个职业信息的点赞
    @Update("update occupation_explode set likes = likes - 1 where id = #{id}")
    void cancelLike(Integer id);

    //    增加某个职业信息的收藏
    @Update("update occupation_explode set collection = collection + 1 where id = #{id}")
    void addCollection(Integer id);

    //    减少某个职业信息的收藏
    @Update("update occupation_explode set collection = collection - 1 where id = #{id}")
    void cancelCollection(Integer id);

    @Insert("INSERT INTO to_do (user_id,task,start_time,end_time,finish,des,stage)values (#{userId},#{task},#{startTime},#{endTime},#{finish},#{des},#{stage})")
    int addPlan(ToDo toDo);


    @Select("SELECT * FROM to_do WHERE user_id = #{userId} and stage = #{stage}")
    ToDo getPlan(Integer userId,Integer stage);
//    根据id去获取计划

    @Update("update to_do set task = #{task},start_time = #{startTime},end_time = #{endTime},finish = #{finish},des = #{des} where user_id = #{userId} and stage = #{stage}")
    int updatePlan(ToDo toDo);


//    获取通过内容获取详细价值观
    @Select("SELECT * FROM occupational_values WHERE values_des= #{values}")
    OccupationValues getOccupationValuesByValues(String values);

//   获取用户的保存进度,这里返回的默认最大关卡，也就是目前的最大记录，

    @Select("SELECT * FROM values_progress where user_id = #{userId} ORDER BY progress DESC")
    List<PersonalProgress> getPersonalProgress(Integer userId);

//    存入进度
    @Insert("insert into values_progress (user_id,progress,explode_values) VALUES (#{userId}, #{progress}, #{explodeValues})")
    int addPersonalProgress(PersonalProgress progress);

//   获取是否已经有数据
    @Select("select  * from values_progress where user_id = #{userId} and progress = #{progress}")
    PersonalProgress getProgress(PersonalProgress progress);

    @Update("update values_progress set progress = #{progress},explode_values=#{explodeValues} where user_id = #{userId}")
    int updateProgress(PersonalProgress progress);

//    插入点赞表
    @Insert("insert into occupation_like (user_id,explode_id,create_time,status) values(#{userId},#{explodeId},#{createTime},#{status})")
    int insertLike(OccupationLike occupationLike);

    @Select("select * from occupation_like where user_id = #{userId} and explode_id = #{explodeId}")
    OccupationLike getLike(Integer userId, Integer explodeId);


    @Select("update occupation_like  set status = #{status} where user_id = #{userId} and explode_id = #{explodeId}")
    OccupationLike updateLike(Integer userId, Integer explodeId,Integer status);
    //    插入收藏表
    @Insert("insert into occupation_collection  (user_id,explode_id,create_time,status) values(#{userId},#{explodeId},#{createTime},#{status})")
    int insertCollection(OccupationCollection occupationCollection);


    @Select("select * from occupation_collection where user_id = #{userId} and explode_id = #{explodeId}")
    OccupationCollection getCollection(Integer userId, Integer explodeId);


    @Select("update occupation_collection  set status = #{status} where user_id = #{userId} and explode_id = #{explodeId}")
    OccupationCollection updateCollection(Integer userId, Integer explodeId,Integer status);

//    获取个人点赞
//@Select("SELECT * from occupation_like  join occupation_explode on\n" +
//        "    occupation_like.explode_id = occupation_explode.id where user_id = #{userId} and status = 1")
    List<OccupationLike> getMyLike(@Param("userId")Integer userId);

    //    获取个人收藏
//    @Select("SELECT * from occupation_collection  join occupation_explode on\n" +
//            "    occupation_collection.explode_id = occupation_explode.id where user_id = #{userId} and status = 1")

    List<OccupationCollection> getMyCollection(@Param("userId")Integer userId);

//    增加直线的次数
    @Update("update to_do set count = count + 1  where user_id = #{userId} and stage = #{stage}")
    int addCount(Integer userId,Integer stage);
}
