package com.juxtapose.example.own.reader;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class MyJdbcPagingItemReader2 extends JdbcPagingItemReader implements Partitioner {

    private static final String _MINRECORD = "_minRecord";
    private static final String _MAXRECORD = "_maxRecord";
    private static final String MIN_SELECT_PATTERN = "select min({0}) from {1}";
    private static final String MAX_SELECT_PATTERN = "select max({0}) from {1}";
    private JdbcTemplate jdbcTemplate ;//根据dataSource来初始化
    private DataSource myDataSource;//xml中配置
    private String table ;//xml中配置
    private String column;//xml中配置

    public Map<String, ExecutionContext> partition(int gridSize) {
        validateAndInit();
        Map<String, ExecutionContext> resultMap = new HashMap<String, ExecutionContext>();
        //根据主键计算最小值
        int min = jdbcTemplate.queryForInt(MessageFormat.format(MIN_SELECT_PATTERN, new Object[]{column,table}));
        int max = jdbcTemplate.queryForInt(MessageFormat.format(MAX_SELECT_PATTERN, new Object[]{column,table}));
        //按照gridSize分成几个小块
        int targetSize = (max-min)/gridSize +1;
        int number=0;
        int start =min;
        int end = start+targetSize-1;

        while(start <= max){
            ExecutionContext context = new ExecutionContext();
            if(end>=max){
                end=max;
            }
            context.putInt(_MINRECORD, start);
            context.putInt(_MAXRECORD, end);
            start+=targetSize;
            end+=targetSize;
            resultMap.put("partition"+(number++), context);
        }
        return resultMap;
    }

    public void validateAndInit(){
        if(isEmpty(table)){
            throw new IllegalArgumentException("table cannot be null");
        }
        if(isEmpty(column)){
            throw new IllegalArgumentException("column cannot be null");
        }
        if(myDataSource!=null && jdbcTemplate==null){
            jdbcTemplate = new JdbcTemplate(myDataSource);
        }
        if(jdbcTemplate==null){
            throw new IllegalArgumentException("jdbcTemplate cannot be null");
        }
    }

    public static boolean isEmpty(String info){
        if(info!=null){
            if(info.trim().length()>1){
                return false;
            }
        }
        return true;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DataSource getMyDataSource() {
        return myDataSource;
    }

    public void setMyDataSource(DataSource myDataSource) {
        this.myDataSource = myDataSource;
    }
}
