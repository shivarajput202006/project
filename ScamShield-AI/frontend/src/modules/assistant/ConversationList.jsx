import ConversationItem from "./ConversationItem.jsx";

export default function ConversationList({ conversations, currentId, onSelect, onDelete, loading, onLoadMore, hasMore }) {
  if (loading && !conversations.length) return <p className="assistant-history-status">Loading conversations…</p>;
  if (!conversations.length) return <p className="assistant-history-status">Your saved conversations will appear here.</p>;
  return (
    <div className="assistant-conversation-list">
      {conversations.map((conversation) => (
        <ConversationItem
          key={conversation.id}
          conversation={conversation}
          active={currentId === conversation.id}
          onSelect={() => onSelect(conversation.id)}
          onDelete={() => onDelete(conversation.id)}
        />
      ))}
      {hasMore && <button className="assistant-load-more" type="button" onClick={onLoadMore}>Load more</button>}
    </div>
  );
}
