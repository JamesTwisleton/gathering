/* Class defines an input buffer used by the game engine */

export class InputBuffer {
  keyW = false;
  keyA = false;
  keyS = false;
  keyD = false;

  getCardinalDirection() {
    if (!this.keyW && !this.keyA && !this.keyS && !this.keyD) {
      return null;
    } else if (this.keyW && !this.keyS) {
      if (this.keyA && !this.keyD) {
        return "NORTHWEST";
      } else if (this.keyD && !this.keyA) {
        return "NORTHEAST";
      } else {
        return "NORTH";
      }
    } else if (this.keyS && !this.keyW) {
      if (this.keyA && !this.keyD) {
        return "SOUTHWEST";
      } else if (this.keyD && !this.keyA) {
        return "SOUTHEAST";
      } else {
        return "SOUTH";
      }
    } else if (this.keyA && !this.keyD) {
      return "WEST";
    } else if (this.keyD && !this.keyA) {
      return "EAST";
    }
  }

  clear() {
    this.keyW = false;
    this.keyA = false;
    this.keyS = false;
    this.keyD = false;
  }
}
