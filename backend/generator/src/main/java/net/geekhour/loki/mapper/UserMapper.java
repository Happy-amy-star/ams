package net.geekhour.loki.mapper;

import net.geekhour.loki.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户 Mapper 接口
 * </p>
 *
 * @author Jasper Yang
 * @since 2025-04-20
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
