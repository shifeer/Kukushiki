package ru.troyanov.opdkukushiki.exceptionHandlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.troyanov.opdkukushiki.dto.exceptionDto.AudioFileExceptionDto;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

@RestControllerAdvice
public class ExceptionHandlers {

//    @ExceptionHandler(value = UnsupportedAudioFileException.class)
//    public ResponseEntity<AudioFileExceptionDto> handleUnsupportedAudioFileException(UnsupportedAudioFileException e) {
//
//        AudioFileExceptionDto audioFileExceptionDto = new AudioFileExceptionDto("Your format file not supported. Send file in format(wav, mp3, ogg)");
//
//       return new ResponseEntity<>(audioFileExceptionDto, HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler(value = IOException.class)
    public ResponseEntity<AudioFileExceptionDto> handleIOException(IOException e) {

        AudioFileExceptionDto audioFileExceptionDto = new AudioFileExceptionDto("Error of server");

        return new ResponseEntity<>(audioFileExceptionDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    @ExceptionHandler(value = InterruptedException.class)
//    public ResponseEntity<AudioFileExceptionDto> handleInterruptedException(InterruptedException e) {
//
//        AudioFileExceptionDto audioFileExceptionDto = new AudioFileExceptionDto("Interrupted error");
//
//        return new ResponseEntity<>(audioFileExceptionDto, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
}
