export function WorldBuilder(worldMessage) {
  return {
    maxX: worldMessage.message.maxX,
    maxY: worldMessage.message.maxY,
    users: worldMessage.message.users
  }
}

export default WorldBuilder;
