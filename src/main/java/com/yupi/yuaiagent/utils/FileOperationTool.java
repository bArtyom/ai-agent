package com.yupi.yuaiagent.utils;

import cn.hutool.core.io.FileUtil;
import com.yupi.yuaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class FileOperationTool {
    private final String FILE_DIR= FileConstant.FILE_SAVE_DIR+"/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileName){
        String filePath=FILE_DIR+"/"+fileName;
        return FileUtil.readUtf8String(filePath);
    }

    
    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write")String fileName,
    @ToolParam(description = "Content to write into the file") String content){
        String filePath=FILE_DIR+"/"+fileName;
        FileUtil.mkdir(FILE_DIR);
        FileUtil.writeUtf8String(content,filePath);
        return "File written successfully: "+filePath;
    }
}
