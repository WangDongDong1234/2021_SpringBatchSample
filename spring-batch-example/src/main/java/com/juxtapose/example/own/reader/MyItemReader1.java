package com.juxtapose.example.own.reader;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.ReaderNotOpenException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.DefaultBufferedReaderFactory;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.NonTransientFlatFileException;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;

public class MyItemReader1<T> implements ItemReader<T>, ItemStream  {

    private Resource resource;//数据源

    private BufferedReader reader;

    private int lineCount=0;//统计行数，每读一行就加1

    private boolean skipRead=false;  //是否需要跳读

    private String encoding="UTF-8";  //读取文件的默认编码

    private LineMapper<T> lineMapper;  //将一行记录转成java对象

    private int linesToSkip=0;  //定义跳过文件的行数；跳过的记录将会被传递给skippedLinesCallback,执行跳过行的回调逻辑

    private LineCallbackHandler skippedLinesCallback;//和linesToSkip搭配

    private int currentItemCount=0;//实际读了几行

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public LineMapper<T> getLineMapper() {
        return lineMapper;
    }

    public void setLineMapper(LineMapper<T> lineMapper) {
        this.lineMapper = lineMapper;
    }

    public LineCallbackHandler getSkippedLinesCallback() {
        return skippedLinesCallback;
    }

    public void setSkippedLinesCallback(LineCallbackHandler skippedLinesCallback) {
        this.skippedLinesCallback = skippedLinesCallback;
    }

    public int getLinesToSkip() {
        return linesToSkip;
    }

    public void setLinesToSkip(int linesToSkip) {
        this.linesToSkip = linesToSkip;
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        currentItemCount++;
        T item=doRead();
        System.out.println("lineCount："+lineCount+"，currentItemCount:"+currentItemCount);
        return item;
    }

    private T doRead() {
        if (skipRead) {
            return null;
        }

        String line = readLine();

        if (line == null) {
            return null;
        }
        else {
            try {
                return lineMapper.mapLine(line, lineCount);
            }
            catch (Exception ex) {
                System.out.println("doRead()方法中出现异常");
                throw new FlatFileParseException("Parsing error at line: " + lineCount + " in resource=["
                        + resource.getDescription() + "], input=[" + line + "]", ex, line, lineCount);
            }
        }
    }

    private String readLine() {
        if (reader == null) {
            throw new ReaderNotOpenException("Reader must be open before it can be read.");
        }

        String line = null;

        try {
            line = this.reader.readLine();
            if (line == null) {
                return null;
            }
            lineCount++;

        }
        catch (IOException e) {
            // Prevent IOException from recurring indefinitely
            // if client keeps catching and re-calling
            skipRead = true;
            throw new NonTransientFlatFileException("Unable to read from resource: [" + resource + "]", e, line,
                    lineCount);
        }
        return line;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            doOpen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doOpen() throws Exception {
        skipRead = true;
        reader = new DefaultBufferedReaderFactory().create(resource, encoding);
        System.out.println("创建reader");
        for (int i = 0; i < linesToSkip; i++) {
            String line = readLine();
            if (skippedLinesCallback != null) {
                skippedLinesCallback.handleLine(line);
            }
        }
        skipRead = false;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        doUpdate();
    }

    private void doUpdate() {
    }

    @Override
    public void close() throws ItemStreamException {
        lineCount = 0;
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
