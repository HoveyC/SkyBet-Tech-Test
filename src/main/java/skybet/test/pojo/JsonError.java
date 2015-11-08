/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybet.test.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents an error message when the json object cannot be parsed.
 * 
 * @author chrishovey
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonError extends JsonSerialisable {

    private int errorCode;
    private String errorMessage;

}
