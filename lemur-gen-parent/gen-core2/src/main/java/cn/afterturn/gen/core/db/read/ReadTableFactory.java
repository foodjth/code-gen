package cn.afterturn.gen.core.db.read;

import cn.afterturn.gen.core.db.exception.GenerationRunTimeException;
import cn.afterturn.gen.core.db.read.impl.ReadTableForMysqlImpl;
import cn.afterturn.gen.core.db.read.impl.ReadTableForOracleImpl;

import static cn.afterturn.gen.core.GenCoreConstant.*;

/**
 * 读取库的工厂
 *
 * @author JueYue
 * @date 2014年12月25日
 */
public class ReadTableFactory {

    public static IReadTable getReadTable(String dbType) {
        if (MYSQL.equalsIgnoreCase(dbType)) {
            return new ReadTableForMysqlImpl();
        }else if (ORACLE.equalsIgnoreCase(dbType)){
            return new ReadTableForOracleImpl();
        }
        throw new GenerationRunTimeException("数据库不支持");
    }

    public static String getDeiver(String dbType) {
        if (MYSQL.equalsIgnoreCase(dbType)) {
            return "com.mysql.jdbc.Driver";
        }else if(ORACLE.equalsIgnoreCase(dbType)){
            return "oracle.jdbc.driver.OracleDriver";
        }
        throw new GenerationRunTimeException("数据库不支持");
    }
}
