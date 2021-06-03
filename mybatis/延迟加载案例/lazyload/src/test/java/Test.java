import com.lazyload.mapper.UserMapper;
import com.lazyload.pojo.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;

import java.io.InputStream;
import java.util.List;

public class Test {
    private InputStream in;
    private SqlSession session;

    private UserMapper userDao;
    private SqlSessionFactory factory;
    @Before
    public void init()throws Exception{
        //获取配置文件
        in = Resources.getResourceAsStream("sqlMapConfig.xml");
        //获取工厂
        factory = new SqlSessionFactoryBuilder().build(in);

        session = factory.openSession();

        userDao = session.getMapper(UserMapper.class);
    }
    @After
    public void destory()throws Exception{
        session.commit();
        session.close();
        in.close();
    }
    @org.junit.Test
    public void findAllTest(){
        List<User> userList = userDao.findAllby();
        for (User user: userList){
            System.out.println("每个用户的信息");
            System.out.println(user);
            System.out.println(user.getAccountList());
        }
    }
}
