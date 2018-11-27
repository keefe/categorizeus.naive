package us.categorize.naive;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import us.categorize.api.MessageStore;
import us.categorize.model.Message;
import us.categorize.model.Tag;
import us.categorize.model.User;

public class NaiveMessageStore implements MessageStore {

	private Connection connection;
		
	public NaiveMessageStore(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public Message createMessage(Message message) {
     	String insert = "insert into messages(body,title,posted_by, replies_to, root_replies_to) values (?,?,?,?,?)";
		try {
			PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
			mapMessageUpdate(message, stmt);
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();
			long key = rs.getLong(1);
			message.setId(key);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return message; 	
	}
	
    private void mapMessageUpdate(Message message, PreparedStatement stmt) throws SQLException
    {
    	stmt.setString(1, message.getBody());
		stmt.setString(2, message.getTitle());
		stmt.setLong(3, message.getPostedBy());
		if(message.getRepliesTo()==0) {
			stmt.setNull(4, Types.BIGINT);
		}else {
			stmt.setLong(4, message.getRepliesTo());			
		}
		if(message.getRootRepliesTo()==0) {
			stmt.setNull(5, Types.BIGINT);
		}else {
			stmt.setLong(5, message.getRootRepliesTo());			
		}
    }

	@Override
	public Message[] tagSearch(String[] tagStrings) {
		Tag[] tags = tagsToObjects(tagStrings);
		return null;
	}


	@Override
	public Message readMessage(long id) {
        try{
            String findMessage = "select * from messages where id=?";
    		PreparedStatement stmt = connection.prepareStatement(findMessage);
    		stmt.setLong(1, id);
    		ResultSet rs = stmt.executeQuery();
    		if(rs.next()){
    		    return mapMessageRow(new Message(), rs);
    		}else{
    		    return null;
    		}
    		/*
    		//continuing to be extremely inefficient, let's do ANOTHER query
    		//curious if doing another query vs doing a join above would lead to more efficient results
    		String findTags = "select * from message_tags, tags where message_tags.tag_id=tags.id AND message_id = ?";
    		PreparedStatement findTagsStmt = connection.prepareStatement(findTags);
    		findTagsStmt.setLong(1, message.getId());
    		rs = findTagsStmt.executeQuery();
    		while(rs.next()){
    			Tag tag = new Tag(rs.getLong("id"), rs.getString("tag"));
    			message.getTags().add(tag);
    		}
    		if(!read(message.getPostedBy())){
    			System.out.println("Message Poster Not Found in Database " + message.getPostedBy());
    		}
    		return true;*/
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }
        return null;
	}
	
    private Message mapMessageRow(Message message, ResultSet rs) throws SQLException {
		message.setBody(rs.getString("body"));
		message.setTitle(rs.getString("title"));
		message.setPostedBy(rs.getLong("posted_by"));
		message.setRepliesTo(rs.getLong("replies_to"));
		message.setRootRepliesTo(rs.getLong("root_replies_to"));
		return message;
	}

	@Override
	public Message[] readMessageThread(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteMessage(long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tagMessage(long id, String[] tags) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addMessageTag(long id, String tag) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeMessageTag(long id, String tag) {
		// TODO Auto-generated method stub
		return false;
	}
	
    public Tag tagFor(String tag){
    	Tag tagObj = new Tag();
    	tagObj.setTag(tag);
    	
    	readOrCreateTag(tagObj);
    	return tagObj;
    }
    
    public boolean readTag(Tag tag){
        try{
    		String findTag = "select * from tags where tag=?";
    		PreparedStatement stmt = connection.prepareStatement(findTag);
    		stmt.setString(1, tag.getTag());
    		ResultSet rs = stmt.executeQuery();
    		if(rs.next()){
    			tag.setId(rs.getLong("id"));
    			return true;
    		}
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }
        return false;
    }
    public boolean readOrCreateTag(Tag tag){
        if(!readTag(tag)){
            try{
         		PreparedStatement insertStatement = connection.prepareStatement("insert into tags(tag) values(?)", Statement.RETURN_GENERATED_KEYS);
    			insertStatement.setString(1, tag.getTag());
    			insertStatement.executeUpdate();//this is synchronous, right?
    			ResultSet rs = insertStatement.getGeneratedKeys();
    			rs.next();
    			long key = rs.getLong(1);
    			tag.setId(key);
    		    return true;
            }catch(SQLException sqe){
                sqe.printStackTrace();
            }
        }
        return false;
        
    }
	private Tag[] tagsToObjects(String tags[]) {
		Tag tag[] = new Tag[tags.length];
		for(int i=0; i<tags.length;i++) {
			tag[i] = tagFor(tags[i]);
		}
		return tag;
	}
}
