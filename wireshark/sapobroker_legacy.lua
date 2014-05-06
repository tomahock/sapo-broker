-- create myproto protocol and its fields
p_brokerproto = Proto ("broker_legacy","Sapo Broker Legacy")
local f_size = ProtoField.uint32("broker.size", "Size", base.DEC)
local f_data = ProtoField.string("broker.data", "Data", FT_STRING)
local f_raw_data = ProtoField.string("broker.data_raw", "Raw Data", FT_STRING)
local f_raw_data_hex = ProtoField.string("broker.data_raw_hex", "Raw Data", base.HEX)

p_brokerproto.fields = {f_size, f_data, f_raw_data, f_raw_data_hex}


-- myproto dissector function
function p_brokerproto.dissector (buf, pkt, root)

    -- validate packet length is adequate, otherwise quit
    if buf:len() == 0 then return end

    pkt.cols.protocol = p_brokerproto.name

    -- create subtree for myproto
    subtree = root:add(p_brokerproto, buf(0))



    if pkt.ipproto == "TCP" then

        local len = 0
        if buf:len() >= 4 then -- change length field size appropriately
            -- we have length field
            len = 0 + buf(0,4):int() -- change appropriately
            if len > buf:len() then
                -- we don't have all the data we need yet
                pkt.desegment_len = len - buf:len() 
                return
            end
        else
            -- we don't have all of length field yet
            pkt.desegment_len = DESEGMENT_ONE_MORE_SEGMENT 
            return
        end
        
        size = buf(0,4)
        --add protocol fields to subtree
        subtree:add(f_size, size)

        data = buf(4,size:int())

    else

        size = buf:len()
        --add protocol fields to subtree
        subtree:add(f_size, size)

        data = buf(0,size)

    end





    


    datatree = subtree:add(f_data)
    datatree:add(f_raw_data, data)

    Dissector.get("xml"):call(data:tvb(), pkt, datatree)

end

-- Initialization routine
function p_brokerproto.init()

end

-- register a chained dissector for port 8002
local tcp_dissector_table = DissectorTable.get("tcp.port")
dissector = tcp_dissector_table:get_dissector(3322)
-- you can call dissector from function p_myproto.dissector above
-- so that the previous dissector gets called
tcp_dissector_table:add(3322, p_brokerproto)

local udp_dissector_table = DissectorTable.get("udp.port")
dissector = udp_dissector_table:get_dissector(3366)
-- you can call dissector from function p_myproto.dissector above
-- so that the previous dissector gets called
udp_dissector_table:add(3366, p_brokerproto)
