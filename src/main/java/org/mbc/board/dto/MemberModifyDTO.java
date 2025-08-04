package org.mbc.board.dto;

import lombok.Data;

@Data
public class MemberModifyDTO {

    private String mid;
    private String mpw;
    private String email;
    private boolean del;
    private boolean social;
}
