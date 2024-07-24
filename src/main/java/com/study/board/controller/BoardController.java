package com.study.board.controller;
import com.study.board.entity.Board;
import com.study.board.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class BoardController {

    @Autowired
    private BoardService boardService;

    @GetMapping("/")
    @ResponseBody // 글자를 그대로 띄워주는 어노테이션
    public String main(){
        return "Hello, Spring Boot!";
    }

    @GetMapping("/board/write") // localhost:8080/board/write
    public String boarWriteForm(){
        return "boardwrite";
    }

    // 게시글 등록
    @PostMapping("/board/writedo")
    // 매개변수로 받음
    public String boardWriteDo(Board board, Model model, @RequestParam("multipartFile") MultipartFile multipartFile)throws Exception {

        // board.getTitle()이럴때 필요한게 board엔티티 lombok에 @Data어노테이션
//        System.out.println("제목 : " + board.getTitle());

        boardService.write(board, multipartFile);

        model.addAttribute("message", "게시글 작성이 완료되었습니다");
        model.addAttribute("searchUrl", "/board/boardList");

        return "message";
    }

    // 게시글 전체 목록 조회 (페이징 처리 X)
//    @GetMapping("/board/boardList")
//    public String getBoardList(
//            Model model
//            ){
//
//        // 반환된 List<Board>를 list라는 이름으로 받아서 넘기겠다.
//        model.addAttribute("list", boardService.boardList());
//
//        return "boardList";
//    }

//    // 게시글 전체 목록 조회
//    @GetMapping("/board/boardList")
//    public String getBoardList(
//            Model model,
//            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC)Pageable pageable
//    ){
//
//        Page<Board> list = boardService.boardList(pageable);
//
//        // 현재 페이지
//        int nowPage = list.getPageable().getPageNumber() + 1;
//        // 매개변수로 들어온 두 값을 비교해 높은 값을 반환하게 된다
//        // nowPage - 4 를 했을때 1보다 작게 나오는 경우에는 둘중에 1이 크기때문에 1을 반환
//        int startPage = Math.max(nowPage - 4, 1);
//        // total 페이지를 넘어버리면 total페이지를 반환할 수 있게
//        int endPage = Math.min(nowPage + 5, list.getTotalPages());
//
//
//        model.addAttribute("list", list);
//        model.addAttribute("nowPage", nowPage);
//        model.addAttribute("startPage", startPage);
//        model.addAttribute("endPage", endPage);
//
//        return "boardList";
//    }

    @GetMapping("/board/boardList")
    public String getBoardList(
            Model model,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword
    ) {

        Page<Board> list = null;

        // 검색어가 없으면(null) 전체 목록,
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            list = boardService.boardList(pageable);
        }else {
            // 있으면 검색결과
            list = boardService.boardSearchList(searchKeyword,pageable);
        }

        int nowPage = list.getPageable().getPageNumber() + 1;
        int totalPages = list.getTotalPages();

        // 현재 페이지 그룹의 시작과 끝
        int pagesPerGroup = 5; // 한 그룹에 표시할 페이지 수
        int startGroupPage = ((nowPage - 1) / pagesPerGroup) * pagesPerGroup + 1;
        int endGroupPage = Math.min(startGroupPage + pagesPerGroup - 1, totalPages);

        // 이전 그룹과 다음 그룹의 시작 페이지
        int previousGroupStartPage = Math.max(startGroupPage - pagesPerGroup, 1);
        int nextGroupStartPage = Math.min(startGroupPage + pagesPerGroup, totalPages + 1);

        // 반환된 List<Board>를 list라는 이름으로 받아서 넘기겠다.
        model.addAttribute("list", list);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startGroupPage", startGroupPage);
        model.addAttribute("endGroupPage", endGroupPage);
        model.addAttribute("previousGroupStartPage", previousGroupStartPage);
        model.addAttribute("nextGroupStartPage", nextGroupStartPage);
        model.addAttribute("totalPages", totalPages);

        return "boardList";
    }



    // 게시글 상세 목록 조회
    @GetMapping("/board/boardDetail")
    public String getBoardDetail(
            Model model,
            @RequestParam("id") Integer id
    ){

        model.addAttribute("board",boardService.boardDetail(id));

        return "boardDetail";
    }

    // 상세 게시글 삭제
    @GetMapping("/board/boardDelete")
    public String deleteBoardDetail(
            @RequestParam("id") Integer id,
            Model model
    ){

        boardService.deleteById(id);

        model.addAttribute("message", "게시글 삭제가 완료되었습니다");
        model.addAttribute("searchUrl", "/board/boardList");

        return "message";
    }

    // 상세 게시글 수정
    @GetMapping("/board/modify/{id}")
    public String modifyBoard(
            @PathVariable("id") Integer id,
            Model model
    ){
        model.addAttribute("board", boardService.boardDetail(id));

        return "boardModify";
    }

    @PostMapping("/board/update/{id}")
    public String updateBoard (
            @PathVariable("id") Integer id,
            Board board, Model model, MultipartFile multipartFile
    )throws Exception {
        // 1. 기존에 있는 글을 검색해서 조회해온다.
        Board boardTemp = boardService.boardDetail(id);
        // 2. 기존에 있던 내용을 새로 덮어 씌우기 한다.
        boardTemp.setTitle(board.getTitle());
        boardTemp.setContent(board.getContent());

        // 3. 변경된 게시글 저장
        boardService.write(boardTemp, multipartFile);

        model.addAttribute("message", "게시글 수정이 완료되었습니다");
        model.addAttribute("searchUrl", "/board/boardList");

        return "message";
    }
}
