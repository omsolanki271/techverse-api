package com.om.blog.payloads;

import java.util.List;

public class PostResponse {
    private List<PostDto> content;
    private int pageNumber;
    private int pageSize;
    private int totalElements;
    private int totalPages;
    private boolean lastePage;
}
