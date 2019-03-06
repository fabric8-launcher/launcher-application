using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace {{.dotnet.namespace}}.Models
{
    public class Fruit
    {
        [Required]
        public long Id { get; set; }
        [Required]
        public string Name { get; set; }
        [Required]
        public int Stock { get; set; }

        public override string ToString()
        {
            return "Fruit [" + Id + ", " + Name + ", " + Stock + "]";
        }
    }
}
