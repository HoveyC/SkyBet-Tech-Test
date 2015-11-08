/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybet.test.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents a bet which can be placed using decimal odds.
 * <p>
 * This class is used to serialise and deserialise Json request objects using
 * Jackson library.
 * <p>
 * By performing the serialise and deserialise, I can ensure the format of the
 * json string the service receives and produces are correct.
 * <p>
 * @author chrishovey
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecimalBet extends JsonSerialisable {

    /**
     * 
     */
    @JsonProperty("bet_id")
    private Long betId;
    private Double odds;
    private Integer stake;
}
