package com.study.board.service;

import com.study.board.entity.Board;
import com.study.board.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Transactional
    public void write(Board board, MultipartFile multipartFile) throws Exception {

        /* 1. 저장할 경로 지정 (맥북은 /, 윈도우는 \\) */
        String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/files";

        /* 2. 파일 이름에 붙이는 이름을 랜덤으로 생성 */
        UUID uuid = UUID.randomUUID();

        /* 3. 그 랜덤이름을 파일name앞에다가 붙이고 _ 다음에 지금 들어온 파일 이름을 붙이고 저장될 파일이름 생성 */
        String originalFileName = uuid + "_" + multipartFile.getOriginalFilename();

        /* 4. 저장될 파일이름 생성 */
        File saveFile = new File(projectPath, originalFileName);

        /* 5. 상단에 예외 처리 필수 (throws Exception) */
        multipartFile.transferTo(saveFile);

        board.setFilename(originalFileName);
        board.setFilepath("/files/" + originalFileName);

        boardRepository.save(board);
    }

    /* 페이징처리를 하지 않을때는 Page를 List로 변경 */
    public Page<Board> boardList(Pageable pageable) {

         return boardRepository.findAll(pageable);
    }

    /* 검색기능 */
    public Page<Board> boardSearchList(String searchKeyword, Pageable pageable) {
        return boardRepository.findByTitleContaining(searchKeyword, pageable);
    }

    public Board boardDetail(Integer id) {

        // optional로 받아야한다.
        return boardRepository.findById(id).get();
    }

    public void deleteById(Integer id) {

        boardRepository.deleteById(id);
    }

}
