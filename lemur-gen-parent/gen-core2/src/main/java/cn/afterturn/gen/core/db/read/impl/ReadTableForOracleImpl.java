package cn.afterturn.gen.core.db.read.impl;

import cn.afterturn.gen.core.db.exception.GenerationRunTimeException;
import cn.afterturn.gen.core.db.read.BaseReadTable;
import cn.afterturn.gen.core.db.read.IReadTable;
import cn.afterturn.gen.core.model.GenBeanEntity;
import cn.afterturn.gen.core.model.enmus.DBType;
import cn.afterturn.gen.core.util.NameUtil;
import cn.afterturn.gen.core.util.TableHandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassNameReadTableForOracleImpl
 * @Description
 * @Author jth
 * @Date2019/10/12 14:29
 * @Version V1.0
 **/
public class ReadTableForOracleImpl extends BaseReadTable implements IReadTable {
    private static String TABLE_SQL = "SELECT A.TABLE_NAME,B.COMMENTS AS TABLE_COMMENT FROM ALL_TABLES A,ALL_TAB_COMMENTS B WHERE A.TABLE_NAME = B.TABLE_NAME AND A.TABLE_NAME = '%S' AND A.OWNER = '%S'";
    private static String ALL_TABLE_SQL = "SELECT A.TABLE_NAME,B.COMMENTS AS TABLE_COMMENT FROM ALL_TABLES A,ALL_TAB_COMMENTS B WHERE A.TABLE_NAME = B.TABLE_NAME AND A.OWNER = '%S'";

    private static String FIELDS_SQL = "SELECT\n" +
            "       CASE WHEN D.CONSTRAINT_NAME IS NOT NULL THEN 1 ELSE 0 END ISKEY, \n" +
            "       A.COLUMN_NAME AS FIELDNAME,\n" +
            "       A.DATA_TYPE AS FIELDTYPE,\n" +
            "       B.COMMENTS AS COLUMN_COMMENT,\n" +
            "       A.DATA_PRECISION AS NUMERIC_PRECISION, \n" +
            "       A.DATA_SCALE AS SCALE,\n" +
            "       A.DATA_LENGTH AS CHARMAXLENGTH,\n" +
            "       A.NULLABLE AS NULLABLE \n" +
            "FROM ALL_TAB_COLUMNS A \n" +
            "LEFT JOIN ALL_COL_COMMENTS B ON A.TABLE_NAME = B.TABLE_NAME AND A.OWNER = B.OWNER AND A.COLUMN_NAME =B.COLUMN_NAME\n" +
            "LEFT JOIN ALL_CONSTRAINTS C ON B.TABLE_NAME = C.TABLE_NAME AND C.CONSTRAINT_TYPE ='P' AND B.OWNER = C.OWNER  \n" +
            "LEFT JOIN ALL_CONS_COLUMNS D ON C.CONSTRAINT_NAME =D.CONSTRAINT_NAME AND B.COLUMN_NAME = D.COLUMN_NAME\n" +
            "WHERE A.TABLE_NAME = '%S' AND A.OWNER = '%S'";

    private static String SCHEMA_SQL = "SELECT USERNAME AS SCHEMA_NAME FROM SYS.DBA_USERS ";

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadTableForMysqlImpl.class);

    @Override
    public GenBeanEntity read(String dbName, String tableName) {
        try {
            GenBeanEntity entity = getTableEntiy(dbName, tableName, TABLE_SQL);
            entity.setName(NameUtil.getEntityHumpName(entity.getTableName()));
            entity.setFields(getTableFields(dbName, tableName, FIELDS_SQL));
            TableHandlerUtil.handlerFields(entity.getFields(), DBType.ORACLE);
            return entity;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new GenerationRunTimeException("获取表格数据发生异常");
        }
    }

    @Override
    public List<String> getAllDB() {
        try {
            return getAllDB(SCHEMA_SQL);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new GenerationRunTimeException("获取表格数据发生异常");
        }
    }

    @Override
    public List<GenBeanEntity> getAllTable(String dbName) {
        try {
            return getAllTableEntiy(dbName, ALL_TABLE_SQL);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new GenerationRunTimeException("获取表格数据发生异常");
        }
    }

    @Override
    protected String handlerTableComment(String comment) {
        if (comment.contains(";")) {
            return comment.split(";")[0];
        }
        if (comment.startsWith("InnoDB free")) {
            return null;
        }
        return comment;
    }
}
