package ru.troyanov;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileMessageDto {
    private String taskId;
    private String fileName;
    private String fileContent;

    public FileMessageDto() {
    }

    public FileMessageDto(String taskId, String fileName,String fileContent) {
        this.taskId = taskId;
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

}