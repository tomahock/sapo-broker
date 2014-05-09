
local path = persconffile_path('plugins') .. "/sapobroker/"

JSON = (loadfile(path.."JSON.lua") )()

-- create myproto protocol and its fields
p_brokerproto = Proto ("broker","Sapo Broker")
local f_encoding = ProtoField.uint16("broker.encoding", "Encoding Type", base.DEC)
local f_encoding_version = ProtoField.uint16("broker.encoding_version", "Encoding Version", base.DEC)
local f_size = ProtoField.uint32("broker.size", "Size", base.DEC)
local f_data = ProtoField.string("broker.data", "Data", FT_STRING)
local f_action_type = ProtoField.string("broker.action_type", "Action Type", FT_STRING)
local f_raw_data = ProtoField.string("broker.data_raw", "Raw Data", FT_STRING)
local f_raw_data_hex = ProtoField.string("broker.data_raw_hex", "Raw Data", base.HEX)

p_brokerproto.fields = {f_encoding, f_encoding_version, f_size, f_data, f_raw_data, f_raw_data_hex,f_action_type}


-- myproto dissector function
function p_brokerproto.dissector (buf, pkt, root)

    -- validate packet length is adequate, otherwise quit
    if buf:len() == 0 then return end

    pkt.cols.protocol = p_brokerproto.name

    -- create subtree for myproto
    subtree = root:add(p_brokerproto, buf(0))

    local len = 0
    if buf:len() >= 8 then -- change length field size appropriately
        -- we have length field
        len = 8 + buf(4,4):int() -- change appropriately
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






    size = buf(4,4)
    enc_type = buf(0,2):int()
    -- add protocol fields to subtree
    subtree:add(f_encoding, buf(0,2))
    subtree:add(f_encoding_version, buf(2,2))
    subtree:add(f_size, size)

    data = buf(8,size:int())




    if enc_type == 3 or enc_type == 0 then
        datatree = subtree:add(f_data)
        datatree:add(f_raw_data, data)

    end

    if enc_type == 3 then


        local package = JSON:decode(data:string())

        subtree:add(f_action_type, package.action.action_type)

        Dissector.get("json"):call(data:tvb(), pkt, datatree)
        pkt.cols.protocol = p_brokerproto.name .. "/JSON"
    end

    if enc_type == 0 then
        Dissector.get("xml"):call(data:tvb(), pkt, datatree)
    end

    if enc_type == 1 then
        pkt.cols.protocol = p_brokerproto.name .. "/PROTOBUF"
    end


    if enc_type == 2 then
        pkt.cols.protocol = p_brokerproto.name .. "/THRIFT"
    end

    -- description of payload
    -- subtree:append_text(", Command details here or in the tree below")

end

-- Initialization routine
function p_brokerproto.init()

end

-- register a chained dissector for port 8002
local tcp_dissector_table = DissectorTable.get("tcp.port")
dissector = tcp_dissector_table:get_dissector(3323)
-- you can call dissector from function p_myproto.dissector above
-- so that the previous dissector gets called
tcp_dissector_table:add(3323, p_brokerproto)

local udp_dissector_table = DissectorTable.get("udp.port")
dissector = udp_dissector_table:get_dissector(3323)
-- you can call dissector from function p_myproto.dissector above
-- so that the previous dissector gets called
udp_dissector_table:add(3323, p_brokerproto)
udp_dissector_table:add(3366, p_brokerproto)
