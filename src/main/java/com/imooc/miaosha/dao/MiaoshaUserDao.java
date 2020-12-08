package com.imooc.miaosha.dao;

import com.imooc.miaosha.domain.MiaoshaUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MiaoshaUserDao {
	
	@Select("select * from miaosha_user where id = #{id}")
	public MiaoshaUser getById(@Param("id")long id);

	@Update("update miaosha_user set password=#{password} where id = #{id}")
	public void update(MiaoshaUser toBeUpdate);

//	"insert into miaosha_user(login_count, nickname, register_date, salt, password, id)values(?,?,?,?,?,?)";
	@Insert("insert into miaosha_user(login_count, nickname, register_date, salt, password, id)values(#{loginCount},#{nickname},#{registerDate},#{salt},#{password}, #{id})")
	public void insert(MiaoshaUser user);
}
