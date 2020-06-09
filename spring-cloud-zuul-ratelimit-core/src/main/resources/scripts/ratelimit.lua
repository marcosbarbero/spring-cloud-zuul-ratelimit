local current = redis.call('incrby', KEYS[1], ARGV[1])

if tonumber(current) == tonumber(ARGV[1]) then
  redis.call('expire', KEYS[1], ARGV[2])
end

return current